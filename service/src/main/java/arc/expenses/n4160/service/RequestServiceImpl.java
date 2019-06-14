package arc.expenses.n4160.service;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.acl.ArcPermission;
import arc.expenses.n4160.domain.NormalStages;
import arc.expenses.n4160.domain.OrderByField;
import arc.expenses.n4160.domain.OrderByType;
import arc.expenses.n4160.domain.RequestSummary;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import eu.openminted.registry.core.service.ServiceException;
import eu.openminted.store.restclient.StoreRESTClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("requestService")
public class RequestServiceImpl extends GenericService<Request> {

    private static Logger logger = LogManager.getLogger(RequestServiceImpl.class);

    @Autowired
    private StoreRESTClient storeRESTClient;

    @Autowired
    private RequestApprovalServiceImpl requestApprovalService;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private InstituteServiceImpl instituteService;

    @Autowired
    private OrganizationServiceImpl organizationService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MailService mailService;

    @Autowired
    private AclService aclService;

    @Autowired
    private TransitionService transitionService;

    @Autowired
    private RequestPaymentServiceImpl requestPaymentService;

    @Value("#{'${admin.emails}'.split(',')}")
    private List<String> admins;


    public RequestServiceImpl() {
        super(Request.class);
    }

    @Override
    public String getResourceType() {
        return "request";
    }


    public String generateID() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date())+"-"+getMaxID();
    }


    @Override
    public Request get(String id) {
        return super.get(id);
    }

    public Request add(Request.Type type, String projectId, String subject, Request.RequesterPosition requesterPosition, String supplier, Stage1.SupplierSelectionMethod supplierSelectionMethod, double amount, Optional<List<MultipartFile>> files, String destination, String firstName, String lastName, String email, int cycles, boolean onBehalf) throws Exception {

        if((type == Request.Type.REGULAR || type == Request.Type.SERVICES_CONTRACT) && supplierSelectionMethod ==null)
            throw new ServiceException("Supplier selection method cannot be empty");

        if(type == Request.Type.REGULAR || type == Request.Type.SERVICES_CONTRACT) {
            if (supplierSelectionMethod != Stage1.SupplierSelectionMethod.AWARD_PROCEDURE && supplier.isEmpty())
                throw new ServiceException("Supplier cannot be empty");

            if (((supplierSelectionMethod == Stage1.SupplierSelectionMethod.AWARD_PROCEDURE || supplierSelectionMethod == Stage1.SupplierSelectionMethod.MARKET_RESEARCH) || amount > 2500) && !files.isPresent())
                throw new ServiceException("Files must be included");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Request request = new Request();
        request.setType(type);
        request.setId(generateID());
        request.setArchiveId(createArchive());
        request.setPaymentCycles(cycles);


        Project project = projectService.get(projectId);
        if(project == null)
            throw new ServiceException("Project with id "+projectId+" not found");
        Institute institute = instituteService.get(project.getInstituteId());
        if(institute == null)
            throw new ServiceException("Institute with id "+ project.getInstituteId()+ " not found");

        Organization organization = organizationService.get(institute.getOrganizationId());
        if(organization == null)
            throw new ServiceException("Organization with id "+ institute.getOrganizationId()+ " not found");

        User user = userService.getByField("user_email",(String) authentication.getPrincipal());

        List<String> pois = new ArrayList<>();

        if(onBehalf) {
            request.setOnBehalfOf(new PersonOfInterest(email,firstName,lastName,new ArrayList<>()));
            pois.add(email);
        }

        request.setUser(user);
        request.setProjectId(projectId);
        request.setRequesterPosition(requesterPosition);
        request.setDiataktis(institute.getDiataktis());

        ArrayList<Attachment> attachments = new ArrayList<>();
        if(files.isPresent()){
            for(MultipartFile file : files.get()){
                String checksum = transitionService.checksum(file.getOriginalFilename());
                storeRESTClient.storeFile(file.getBytes(), request.getArchiveId()+"/", checksum);
                attachments.add(new Attachment(file.getOriginalFilename(), FileUtils.extension(file.getOriginalFilename()),new Long(file.getSize()+""), request.getArchiveId()+"/"+checksum));
            }
        }

        request.setRequestStatus(Request.RequestStatus.PENDING);

        if(!destination.isEmpty()) {
            Trip trip = new Trip();
            trip.setDestination(destination);
            trip.setEmail((email.isEmpty() ? user.getEmail() : email));
            trip.setFirstname((firstName.isEmpty() ? user.getFirstname() : firstName));
            trip.setLastname((lastName.isEmpty() ? user.getLastname() : lastName));
            request.setTrip(trip);
            if (!email.isEmpty()) {
                if (!pois.contains(email))
                    pois.add(email);
            }

        }

        if(type == Request.Type.REGULAR){
            if(!pois.contains(institute.getSuppliesOffice().getEmail()))
                pois.add(institute.getSuppliesOffice().getEmail());

            institute.getSuppliesOffice().getDelegates().forEach(delegate -> {
                if(!pois.contains(delegate.getEmail()))
                    pois.add(delegate.getEmail());
            });
        }else if (type == Request.Type.TRIP){
            if(!pois.contains(institute.getTravelManager().getEmail()))
                pois.add(institute.getTravelManager().getEmail());

            institute.getTravelManager().getDelegates().forEach(delegate -> {
                if(!pois.contains(delegate.getEmail()))
                    pois.add(delegate.getEmail());
            });
        }


        if(!pois.contains(project.getScientificCoordinator().getEmail()))
            pois.add(project.getScientificCoordinator().getEmail());

        if(!pois.contains(user.getEmail()))
            pois.add(user.getEmail());

        for(Delegate delegate : project.getScientificCoordinator().getDelegates())
            if(!pois.contains(delegate.getEmail()))
                pois.add(delegate.getEmail());

        request.setCurrentStage(NormalStages.Stage2.name());
        request.setPois(pois);
        request.setFinalAmount(amount);


        request = super.add(request, authentication);


        Stage1 stage1 = new Stage1(new Date().toInstant().toEpochMilli()+"", amount, subject, supplier, supplierSelectionMethod, amount);
        stage1.setAttachments(attachments);
        stage1.setDate(new Date().toInstant().toEpochMilli());

        RequestApproval requestApproval = createRequestApproval(request);
        requestApproval.setCurrentStage(NormalStages.Stage2.name());
        requestApproval.setStage1(stage1);
        requestApprovalService.update(requestApproval,requestApproval.getId());
        mailService.sendMail("INITIAL", request.getId(), project.getAcronym(), stage1.getRequestDate(), stage1.getFinalAmount()+"", subject, false, requestApproval.getId(), request.getPois());

        return request;
    }


    private RequestApproval createRequestApproval(Request request) {
        logger.debug("Request with id " + request.getId() + " has just been created");

        Project project = projectService.get(request.getProjectId());
        Institute institute = instituteService.get(project.getInstituteId());

        RequestApproval requestApproval = new RequestApproval();
        requestApproval.setId(request.getId()+"-a1");
        requestApproval.setRequestId(request.getId());
        requestApproval.setCreationDate(new Date().toInstant().toEpochMilli());
        requestApproval.setStage("2");
        requestApproval.setStatus(BaseInfo.Status.PENDING);
        requestApproval.setLastModified(new LastModified((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), new Date().toInstant().toEpochMilli()));

        requestApproval = requestApprovalService.add(requestApproval, null);

        try{
            aclService.createAcl(new ObjectIdentityImpl(RequestApproval.class, requestApproval.getId()));
        }catch (AlreadyExistsException ex){
            logger.debug("Object identity already exists");
        }

        AclImpl acl = (AclImpl) aclService.readAclById(new ObjectIdentityImpl(RequestApproval.class, requestApproval.getId()));
        acl.insertAce(acl.getEntries().size(), ArcPermission.CANCEL, new PrincipalSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().toLowerCase()), true);

        acl.insertAce(acl.getEntries().size(), ArcPermission.CANCEL, new GrantedAuthoritySid("ROLE_ADMIN"), true);
        if(request.getOnBehalfOf()!=null)
            acl.insertAce(acl.getEntries().size(), ArcPermission.CANCEL, new PrincipalSid(request.getOnBehalfOf().getEmail().toLowerCase()), true);

        acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new GrantedAuthoritySid("ROLE_ADMIN"), true);
        acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(project.getScientificCoordinator().getEmail()), true);
        acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()), true);
        if(request.getOnBehalfOf()!=null)
            acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(request.getOnBehalfOf().getEmail()), true);

        acl.insertAce(acl.getEntries().size(), ArcPermission.WRITE, new PrincipalSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()), true);

        acl.insertAce(acl.getEntries().size(), ArcPermission.EDIT, new PrincipalSid(project.getScientificCoordinator().getEmail()), true);

        if(request.getType() == Request.Type.REGULAR){
            acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(institute.getSuppliesOffice().getEmail()), true);
            institute.getSuppliesOffice().getDelegates().forEach(delegate -> {
                acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(delegate.getEmail()), true);
            });
        }else if(request.getType() == Request.Type.TRIP){
            acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(institute.getTravelManager().getEmail()), true);
            institute.getTravelManager().getDelegates().forEach(delegate -> {
                acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(delegate.getEmail()), true);
            });
        }
        for(Delegate person : project.getScientificCoordinator().getDelegates())
            acl.insertAce(acl.getEntries().size(), ArcPermission.EDIT, new PrincipalSid(person.getEmail()), true);

        acl.setOwner(new GrantedAuthoritySid(("ROLE_USER")));
        aclService.updateAcl(acl);


        return requestApproval;
    }


    public boolean doesntExceedBudget(PersonOfInterest scientificCoordinator, String projectId, Double amount){

        String budgetQuery = "select CASE WHEN sum(request_final_amount) + "+amount+" >(0.25 * project_view.project_total_cost) THEN false ELSE true END AS canBeDiataktis from request_view INNER JOIN project_view ON project_view.project_id=request_view.request_project WHERE request_view.request_diataktis='"+scientificCoordinator.getEmail()+"' AND project_view.project_id='"+projectId+"' AND request_view.request_status IN ('PENDING','ACCEPTED') GROUP BY project_view.project_total_cost;";

        return new JdbcTemplate(dataSource).query(budgetQuery , rs -> {
            if(rs.next())
                return rs.getBoolean("canbediataktis");
            else
                return true;
        });
    }


    public int getMaxID() {

        return new JdbcTemplate(dataSource).query("select count(*)+1 AS next_id from request_view where creation_date > current_date;", resultSet -> {
            if(resultSet.next())
                return resultSet.getInt("next_id");
            else
                return 1;
        });

    }


    public Paging<RequestSummary> criteriaSearch(int from, int quantity,
                                                 List<BaseInfo.Status> status, List<Request.Type> types, String searchField,
                                                 List<String> stages, OrderByType orderType,
                                                 OrderByField orderField,
                                                 boolean canEdit,
                                                 boolean isMine,
                                                 String projectAcronym,
                                                 String instituteName,
                                                 String requester) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = false;
        for(GrantedAuthority grantedAuthority : authentication.getAuthorities()){
            if(grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                isAdmin = true;
                break;
            }
        }

        String aclEntriesQuery = "select distinct on (d.object_id_identity) d.object_id_identity as id, r.request_requester as requester, d.request_id, r.request_type, d.creation_date, d.stage, d.status, d.canEdit, p.project_acronym, i.institute_id, i.institute_name, p.project_scientificcoordinator, p.project_operator, p.project_operator_delegate" +
                " from (" +
                "select o.object_id_identity, a.stage, a.status, a.request_id, a.creation_date, e.mask, CASE WHEN mask=32 and a.status in ('PENDING','UNDER_REVIEW') THEN true ELSE false END AS canEdit" +
                " from acl_entry e, acl_object_identity o, acl_sid s, approval_view a" +
                " where e.acl_object_identity = o.id and o.object_id_identity=a.approval_id and e.sid = s.id and s.sid in ('"+SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().toLowerCase()+"'"+(isAdmin ? ", 'ROLE_ADMIN'" : "")+" )" +
                "union " +
                "select o.object_id_identity, p.stage, p.status, p.request_id, p.creation_date, e.mask, CASE WHEN mask=32 and p.status in ('PENDING','UNDER_REVIEW') THEN true ELSE false END AS canEdit " +
                " from acl_entry e, acl_object_identity o, acl_sid s, payment_view p" +
                " where e.acl_object_identity = o.id and o.object_id_identity=p.payment_id and e.sid = s.id and s.sid in ('"+SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().toLowerCase()+"'"+(isAdmin ? ", 'ROLE_ADMIN'" : "")+" )" +
                ") d, request_view r, project_view p, institute_view i " +
                "where d.request_id = r.request_id AND r.request_project = p.project_id AND p.project_institute = i.institute_id " +
                "order by object_id_identity, canEdit desc";

        String viewQuery = "SELECT *, count(*) OVER() as totals FROM ("+aclEntriesQuery+") aclQ ";


        viewQuery+= " where "+(projectAcronym.isEmpty() ? "" : "project_acronym ILIKE :projectAcronym and ")+" "+(instituteName.isEmpty() ? "" : " ( institute_name ILIKE :institute OR institute_id ILIKE :institute) and ")+""+(requester.isEmpty() ? " " : " requester ILIKE :requester and ")+" status in ("+status.stream().map(p -> "'"+p.toString()+"'").collect(Collectors.joining(","))+") and request_type in ("+types.stream().map(p -> "'"+p.toString()+"'").collect(Collectors.joining(","))+") "+(canEdit ? "and canEdit=true " : "" )+"and stage in (:stages) "+(!searchField.isEmpty() ? "and (project_scientificcoordinator ILIKE :searchField or :searchField = any(project_operator) or :searchField = any(project_operator_delegate) or request_id=:searchField)" : "")+ (isMine ? " AND requester='"+SecurityContextHolder.getContext().getAuthentication().getPrincipal()+"'" : "")+" order by "+orderField+" "  +  orderType + " offset :offset limit :limit";
        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("searchField",searchField);
        in.addValue("projectAcronym","%"+projectAcronym+"%");
        in.addValue("institute", "%"+instituteName+"%");
        in.addValue("requester", "%"+requester+"%");
        in.addValue("stages",stages);
        in.addValue("offset",from);
        in.addValue("limit",quantity);


        System.out.println(viewQuery);

        return new NamedParameterJdbcTemplate(dataSource).query(viewQuery, in, rs -> {
            List<RequestSummary> results = new ArrayList<>();
            int totals = 0;
            while(rs.next()){
                totals = rs.getInt("totals");
                BaseInfo baseInfo = new BaseInfo();
                if(rs.getString("status") !=null && !rs.getString("status").isEmpty())
                    baseInfo.setStatus(BaseInfo.Status.valueOf(rs.getString("status")));

                if(rs.getString("stage") !=null && !rs.getString("stage").isEmpty())
                    baseInfo.setStage(rs.getString("stage"));

                Request request = get(rs.getString("request_id"));
                Project project = projectService.get(request.getProjectId());
                Institute institute = instituteService.get(project.getInstituteId());

                baseInfo.setId(rs.getString("id"));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                boolean failedToParse = false;
                try {
                    baseInfo.setCreationDate(sdf.parse(rs.getString("creation_date")).getTime());
                } catch (ParseException e) {
                    failedToParse = true;
                }
                if(failedToParse){
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        baseInfo.setCreationDate(sdf.parse(rs.getString("creation_date")).getTime());
                    } catch (ParseException e) {
                        logger.warn("Failed to parse creation date from sql query");
                    }

                }
                baseInfo.setRequestId(request.getId());
                RequestSummary requestSummary = new RequestSummary();

                requestSummary.setBaseInfo(baseInfo);
                requestSummary.setCanEdit(rs.getBoolean("canEdit"));
                requestSummary.setRequestFullName(request.getUser().getFirstname() + " " + request.getUser().getLastname());
                requestSummary.setRequestType(request.getType().toString());
                requestSummary.setProjectAcronym(project.getAcronym());
                requestSummary.setInstituteName(institute.getName());


                results.add(requestSummary);
            }
            return new Paging<>(totals,from, from + results.size(), results, new ArrayList<>());
        });
    }

    public List<Request> getPendingRequests(String email) {

        //language=SQL
        String whereClause = " (  ( r.request_project_operator <@ '{"+'"' + email + '"' + "} or  request_project_operator_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and ( request_stage = 3 ) "
                           + "    ) "
                           + " or ( ( request_institute_suppliesOffice = '" + email + "' or request_institute_suppliesOffice_delegate <@ '{"+'"' + email + '"' + "}')" +
                                    "and request_stage = 7 and request_type != trip ) "
                           + " or ( request_institute_travelManager = '" + email + "' or request_institute_travelManager_delegate <@ '{"+'"' + email + '"' + "}')" +
                                    "' and request_stage = 7 and request_type = trip ) "
                           + " or ( request_project_scientificCoordinator = '" + email + "' and request_stage = 2 ) "
                           + " or ( ( request_organization_poy =  '" + email + "' or  request_organization_poy_delegate = "  + email + " ) "
                           + "      and ( request_stage = 4 or request_stage = 9 ) "
                           + "    ) "
                           + " or ( ( request_institute_director =  " + email + " or  request_institute_director_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and ( request_stage = 5a or request_stage = 10 ) "
                           + "    ) "
                           + " or ( ( request_organization_dioikitikoSumvoulio =  '" + email + "' or  request_organization_dioikitikoSumvoulio_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and request_stage = 5b "
                           + "    ) "
                           + " or ( ( request_institute_diaugeia =  '" + email + "' or  request_institute_diaugeia_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and ( request_stage = 6 or request_stage = 11 ) "
                           + "    ) "
                           + " or ( ( request_organization_inspectionTeam <@ '{"+'"' + email + '"' + "} or  request_organization_inspectionTeam_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and request_stage = 8 "
                           + "    ) "
                           + " or ( ( request_institute_accountingRegistration =  '" + email + "' or  request_institute_accountingRegistration_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and request_stage = 12 "
                           + "    ) "
                           + " or ( ( request_institute_accountingPayment =  '" + email + "' or  request_institute_accountingPayment_delegate <@ '{"+'"' + email + '"' + "}')"
                           + "      and request_stage = 13 "
                           + "    ) "
                            + " or (  request_requester =  '" + email + "' and  request_stage = 7 )";

        logger.info(whereClause);


        Paging<Resource> rs = searchService.cqlQuery(
                whereClause,"request",
                1000,0,"", "ASC");


        List<Request> resultSet = new ArrayList<>();
        for(Resource resource:rs.getResults()) {
            resultSet.add(parserPool.deserialize(resource,typeParameterClass));
        }
        return resultSet;

    }

    public String createArchive() {
        return storeRESTClient.createArchive().getResponse();
    }

    public ResponseEntity<Object> upLoadFile(String mode,String archiveID,
                                             String stage, MultipartFile file) {


        if(!mode.equals("request"))
            archiveID += "/"+mode;

        String fileName = stage;
        if(Boolean.parseBoolean(storeRESTClient.fileExistsInArchive(archiveID,fileName).getResponse()))
            storeRESTClient.deleteFile(archiveID,fileName);

        try {
            storeRESTClient.storeFile(file.getBytes(),archiveID,fileName);
        } catch (IOException e) {
            logger.info(e);
            return new ResponseEntity<>("ERROR",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(archiveID+"/"+fileName,HttpStatus.OK);

    }

    @PreAuthorize("hasPermission(#requestApproval,'READ')")
    public File downloadFile(File file,RequestApproval requestApproval,String url) {
        try {
            storeRESTClient.downloadFile(url, file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            logger.error("error downloading file", e);
        }
        return null;
    }

    @PreAuthorize("hasPermission(#requestPayment,'READ')")
    public File downloadFile(File file,RequestPayment requestPayment,String url) {
        try {
            storeRESTClient.downloadFile(url, file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            logger.error("error downloading file", e);
        }
        return null;
    }

    @PreAuthorize("hasPermission(#requestPayment,'WRITE')")
    public void deleteFile(RequestPayment requestPayment,String archiveId) {
        String[] splitted = archiveId.split("/");
        if(splitted.length!=2)
            throw new ServiceException("Bad archiveId format");
        storeRESTClient.deleteFile(splitted[0], splitted[1]);
    }

    @PreAuthorize("hasPermission(#requestApproval,'WRITE')")
    public void deleteFile(RequestApproval requestApproval,String archiveId) {
        String[] splitted = archiveId.split("/");
        if(splitted.length!=2)
            throw new ServiceException("Bad archiveId format");
        storeRESTClient.deleteFile(splitted[0], splitted[1]);
    }

    public Attachment getAttachmentFromApproval(RequestApproval requestApproval, String archiveId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class> stagesClasses = Arrays.stream(RequestApproval.class.getDeclaredFields()).filter(p-> Stage.class.isAssignableFrom(p.getType())).flatMap(p -> Stream.of(p.getType())).collect(Collectors.toList());
        for(Class stageClass : stagesClasses) {
            if (RequestApproval.class.getMethod("get" + stageClass.getSimpleName()).invoke(requestApproval) != null) {
                Stage stage = (Stage) RequestApproval.class.getMethod("get" + stageClass.getSimpleName()).invoke(requestApproval);
                for(Attachment attachment : stage.getAttachments()){
                    if(attachment.getUrl().equals(archiveId))
                        return attachment;
                }
            }
        }
        return null;
    }

    public Attachment getAttachmentFromPayment(RequestPayment requestPayment, String archiveId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class> stagesClasses = Arrays.stream(RequestPayment.class.getDeclaredFields()).filter(p-> Stage.class.isAssignableFrom(p.getType())).flatMap(p -> Stream.of(p.getType())).collect(Collectors.toList());
        for(Class stageClass : stagesClasses) {
            if (RequestPayment.class.getMethod("get" + stageClass.getSimpleName()).invoke(requestPayment) != null) {
                Stage stage = (Stage) RequestPayment.class.getMethod("get" + stageClass.getSimpleName()).invoke(requestPayment);
                for(Attachment attachment : stage.getAttachments()){
                    if(attachment.getUrl().equals(archiveId))
                        return attachment;
                }
            }
        }
        return null;
    }

    public void updateDiataktis(String id) throws Exception {
        FacetFilter filter = new FacetFilter();
        filter.setQuantity(10000);
        if(!id.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("request_id", id);
            filter.setFilter(map);
        }
        List<Request> requests = getAll(filter,null).getResults();
        updateDiataktis(requests,null);
    }

    public void updateDiataktis(List<Request> requests, Project project) throws Exception {
        int i=0;
        for(Request request : requests){
            if(project == null)
                project = projectService.get(request.getProjectId());
            if(project == null)
                continue;

            Institute institute = instituteService.get(project.getInstituteId());
            Organization organization = organizationService.get(institute.getOrganizationId());

            RequestApproval requestApproval = requestApprovalService.getApproval(request.getId());
            if(requestApproval == null){
                i++;
                continue;
            }
            if(requestApproval.getStage5a()==null && !requestApproval.getStage().equals("5a"))
                request.setDiataktis(institute.getDiataktis());
            else{
                request.setDiataktis(institute.getDiataktis());
                if((!request.getUser().getEmail().equals(project.getScientificCoordinator().getEmail())) && request.getFinalAmount()<=2500  && doesntExceedBudget(project.getScientificCoordinator(),project.getId(), request.getFinalAmount()))
                    request.setDiataktis(project.getScientificCoordinator());

                if(request.getUser().getEmail().equals(request.getDiataktis().getEmail())){
                    if(request.getUser().getEmail().equals(organization.getDirector().getEmail()))
                        request.setDiataktis(organization.getViceDirector());
                    else
                        request.setDiataktis(organization.getDirector());
                }
            }

            update(request,request.getId());
        }

        logger.info(i + " approvals not found");
    }

    public void updatePois(String id) throws ResourceNotFoundException {
        FacetFilter filter = new FacetFilter();
        filter.setQuantity(10000);
        if(!id.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("request_id", id);
            filter.setFilter(map);
        }
        List<RequestApproval> requestApprovals = requestApprovalService.getAll(filter,null).getResults();
        for(RequestApproval requestApproval : requestApprovals) {
            Request request = get(requestApproval.getRequestId());
            if(request == null)
                continue;
            List<String> toAdd = aclService.getPois(requestApproval.getId(),RequestApproval.class);
            List<String> pois = new ArrayList<>();
            for(String adding : toAdd){
                if(!pois.contains(adding))
                    pois.add(adding);
            }
            request.setPois(pois);
            update(request,request.getId());
        }

        List<RequestPayment> requestPayments = requestPaymentService.getAll(filter,null).getResults();
        for(RequestPayment requestPayment : requestPayments) {
            Request request = get(requestPayment.getRequestId());
            if(request == null)
                continue;
            List<String> toAdd = aclService.getPois(requestPayment.getId(),RequestPayment.class);
            List<String> pois = new ArrayList<>();
            for(String adding : toAdd){
                if(!pois.contains(adding))
                    pois.add(adding);
            }
            request.setPois(pois);
            update(request,request.getId());
        }
    }


}
