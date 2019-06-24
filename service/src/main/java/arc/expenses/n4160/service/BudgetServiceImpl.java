package arc.expenses.n4160.service;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.acl.ArcPermission;
import arc.expenses.n4160.domain.*;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import eu.openminted.registry.core.service.ServiceException;
import eu.openminted.store.restclient.StoreRESTClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("budgetService")
public class BudgetServiceImpl extends GenericService<Budget> {

    private static Logger logger = LogManager.getLogger(BudgetServiceImpl.class);

    @Autowired
    private StoreRESTClient storeRESTClient;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RequestServiceImpl requestService;

    @Autowired
    private TransitionService transitionService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private AclService aclService;

    @Autowired
    private InstituteServiceImpl instituteService;

    @Autowired
    @Qualifier("budgetFactory")
    @Lazy
    private StateMachineFactory<BudgetStages, StageEvents> budgetFactory;


    @Value("#{'${admin.emails}'.split(',')}")
    private List<String> admins;


    public BudgetServiceImpl() {
        super(Budget.class);
    }

    @Override
    public String getResourceType() {
        return "budget";
    }


    public String generateID() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date())+"-"+getMaxID()+"-b1";
    }


    private StateMachine<BudgetStages, StageEvents> build(Budget budget){

        StateMachine<BudgetStages, StageEvents> sm = this.budgetFactory.getStateMachine(budget.getId());
        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {

                    sma.addStateMachineInterceptor( new StateMachineInterceptorAdapter<BudgetStages, StageEvents>(){

                        @Override
                        public void postStateChange(State state, Message message, Transition transition, StateMachine stateMachine) {
                            Optional.ofNullable(message).ifPresent(msg -> {
                                Optional.ofNullable((Budget) msg.getHeaders().get("budgetRequest"))
                                        .ifPresent(budget ->{
                                            budget = get(budget.getId());
                                            budget.setLastModified(new LastModified((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), new Date().toInstant().toEpochMilli()));
                                            budget.setCurrentStage(state.getId()+""); // <-- casting to String causes uncertain behavior. Keep it this way
                                            try {
                                                logger.info("Updating budget with id "+ budget.getId()+" to stage " + state.getId());
                                                update(budget, budget.getId());
                                            } catch (ResourceNotFoundException e) {
                                                throw new ServiceException("Budget with id " + budget.getId() + " not found");
                                            }
                                            msg.getHeaders().replace("budgetRequest",budget);
                                        });
                            });
                        }
                    });

                    sma.resetStateMachine(new DefaultStateMachineContext<>(
                            BudgetStages.valueOf((budget.getCurrentStage() == null ? BudgetStages.Stage1.name() : budget.getCurrentStage())), null, null, null));

                    logger.info("Resetting machine of budget " + budget.getId() + " at state " + sm.getState().getId());
                });

        sm.start();
        return sm;
    }


    @PreAuthorize("hasPermission(#budget,'EDIT')")
    public void approve(Budget budget, HttpServletRequest req) {
        logger.info("Approving budget with id " + budget.getId());
        StateMachine<BudgetStages, StageEvents> sm = this.build(budget);
        Message<StageEvents> eventsMessage = MessageBuilder.withPayload(StageEvents.APPROVE)
                .setHeader("budgetRequest", budget)
                .setHeader("restRequest", req)
                .build();

        sm.sendEvent(eventsMessage);
        if(sm.hasStateMachineError())
            throw new ServiceException((String) sm.getExtendedState().getVariables().get("error"));

        sm.stop();

    }


    @PreAuthorize("hasPermission(#budget,'EDIT')")
    public void reject(Budget budget, HttpServletRequest req) {
        logger.info("Rejecting budget with id " + budget.getId());
        StateMachine<BudgetStages, StageEvents> sm = this.build(budget);
        Message<StageEvents> eventsMessage = MessageBuilder.withPayload(StageEvents.REJECT)
                .setHeader("budgetRequest", budget)
                .setHeader("restRequest", req)
                .build();
        sm.sendEvent(eventsMessage);
        if(sm.hasStateMachineError())
            throw new ServiceException((String) sm.getExtendedState().getVariables().get("error"));

        sm.stop();

    }


    @PreAuthorize("hasPermission(#budget,'EDIT')")
    public void downgrade(Budget budget, HttpServletRequest req) {
        logger.info("Downgrading budget with id " + budget.getId());
        StateMachine<BudgetStages, StageEvents> sm = this.build(budget);
        Message<StageEvents> eventsMessage = MessageBuilder.withPayload(StageEvents.DOWNGRADE)
                .setHeader("budgetRequest", budget)
                .setHeader("restRequest", req)
                .build();

        sm.sendEvent(eventsMessage);
        if(sm.hasStateMachineError())
            throw new ServiceException((String) sm.getExtendedState().getVariables().get("error"));
        sm.stop();

    }

    @PreAuthorize("hasPermission(#budget,'CANCEL')")
    public void cancel(Budget budget) {
        logger.info("Canceling budget with id " + budget.getId());
        StateMachine<BudgetStages, StageEvents> sm = this.build(budget);
        Message<StageEvents> eventsMessage = MessageBuilder.withPayload(StageEvents.CANCEL)
                .setHeader("budgetRequest", budget)
                .build();

        sm.sendEvent(eventsMessage);
        if(sm.hasStateMachineError())
            throw new ServiceException((String) sm.getExtendedState().getVariables().get("error"));
        sm.stop();
    }

    @PreAuthorize("hasPermission(#budget,'WRITE')")
    public void edit(Budget budget, HttpServletRequest req) {
        logger.info("Editing budget with id " + budget.getId());
        StateMachine<BudgetStages, StageEvents> sm = this.build(budget);
        Message<StageEvents> eventsMessage = MessageBuilder.withPayload(StageEvents.EDIT)
                .setHeader("budgetRequest", budget)
                .setHeader("restRequest", req)
                .build();

        sm.sendEvent(eventsMessage);
        if(sm.hasStateMachineError())
            throw new ServiceException((String) sm.getExtendedState().getVariables().get("error"));
        sm.stop();

    }

    @PreAuthorize("hasPermission(#budget,'WRITE')")
    public JSONObject amountsOfBudget(Budget budget){

        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("budgetId",budget.getId());
        JSONObject jsonObject = new JSONObject();
        JSONObject innerObject = new JSONObject();

        innerObject.put("total",budget.getRegularAmount());
        String query = "SELECT coalesce(SUM(request_final_amount),0) as sum  from request_view where request_type = 'REGULAR' AND request_status='ACCEPTED' AND request_budget=:budgetId";
        Float result = new NamedParameterJdbcTemplate(dataSource).query(query, in, rs -> {
            rs.next();
            return rs.getFloat("sum");
        });
        innerObject.put("paid",result);
        jsonObject.put("REGULAR",innerObject);
        innerObject = new JSONObject();

        innerObject.put("total",budget.getTripAmount());
        query = "SELECT coalesce(SUM(request_final_amount),0) as sum  from request_view where request_type = 'TRIP' AND request_status='ACCEPTED' AND request_budget=:budgetId";
        result = new NamedParameterJdbcTemplate(dataSource).query(query, in, rs -> {
            rs.next();
            return rs.getFloat("sum");
        });
        innerObject.put("paid",result);
        jsonObject.put("TRIP",innerObject);
        innerObject = new JSONObject();


        innerObject.put("total",budget.getServicesContractAmount());
        query = "SELECT coalesce(SUM(request_final_amount),0) as sum  from request_view where request_type = 'SERVICES_CONTRACT' AND request_status='ACCEPTED' AND request_budget=:budgetId";
        result = new NamedParameterJdbcTemplate(dataSource).query(query, in, rs -> {
            rs.next();
            return rs.getFloat("sum");
        });
        innerObject.put("paid",result);
        jsonObject.put("SERVICES_CONTRACT",innerObject);
        innerObject = new JSONObject();


        innerObject.put("total",budget.getContractAmount());
        query = "SELECT coalesce(SUM(request_final_amount),0) as sum  from request_view where request_type = 'CONTRACT' AND request_status='ACCEPTED' AND request_budget=:budgetId";
        result = new NamedParameterJdbcTemplate(dataSource).query(query, in, rs -> {
            rs.next();
            return rs.getFloat("sum");
        });
        innerObject.put("paid",result);
        jsonObject.put("CONTRACT",innerObject);

        return jsonObject;

    }

    public Float getAmountPerType(Budget budget, String type){
        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("budgetId",budget.getId());
        in.addValue("type",type);

        String query = "SELECT coalesce(SUM(request_final_amount),0) as sum  from request_view where request_type = :type AND request_status='ACCEPTED' AND request_budget=:budgetId";
        return new NamedParameterJdbcTemplate(dataSource).query(query, in, rs -> {
            rs.next();
            return rs.getFloat("sum");
        });
    }

    @PreAuthorize("hasRole('ROLE_OPERATOR') or hasRole('ROLE_ADMIN')")
    public Budget add(String projectId,
                      int year,
                      Double regularAmount,
                      Double contractAmount,
                      Double tripAmount,
                      Double servicesContractAmount,
                      Optional<MultipartFile> boardDecision,
                      Optional<MultipartFile> technicalReport,
                      String comment) throws Exception {

        Project project = projectService.get(projectId);

        User user = userService.getByField("user_email",(String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        if(user == null)
            throw new ServiceException("Logged in user not found in DB...(!)");

        if(project == null)
            throw new ServiceException("Δεν βρέθηκε το έργο");

        if(!boardDecision.isPresent() || !technicalReport.isPresent())
            throw new ServiceException("Παρακαλώ προσθέστε τα απαραίτητα συνημμένα");

        if(regularAmount < 0 || contractAmount < 0 || tripAmount < 0 || servicesContractAmount < 0)
            throw new ServiceException("Δεν επιτρέπονται αρνητικές τιμές στα ποσά");

        if(alreadyExists(project,year))
            throw new ServiceException("Υπάρχει ήδη προυπολογισμός γι αυτό το έτος");

        boolean isProjectsOperator = false;
        for(PersonOfInterest poi : project.getOperator()){
            if (poi.getEmail().equals(user.getEmail())){
                isProjectsOperator=true;
                break;
            }
        }

        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if(!isProjectsOperator && !authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            throw new ServiceException("Ο χρήστης δεν είναι χειριστής του έργου");


        List<String> pois = new ArrayList<>();
        pois.add(user.getEmail());

        String archiveId = requestService.createArchive();

        Budget budget = new Budget();
        budget.setProjectId(projectId);
        budget.setId(generateID());
        budget.setYear(year);
        budget.setRegularAmount(regularAmount);
        budget.setContractAmount(contractAmount);
        budget.setTripAmount(tripAmount);
        budget.setServicesContractAmount(servicesContractAmount);
        budget.setDate(new Date().toInstant().toEpochMilli());
        budget.setSubmittedBy(user);
        budget.setBudgetStatus(Budget.BudgetStatus.PENDING);
        budget.setStage("2");
        budget.setPois(pois);
        budget.setArchiveId(archiveId);
        budget.setComment(comment);

        String checksum = transitionService.checksum(boardDecision.get().getOriginalFilename());
        storeRESTClient.storeFile(boardDecision.get().getBytes(), archiveId+"/", checksum);

        budget.setBoardDecision(new Attachment(boardDecision.get().getOriginalFilename(), FileUtils.extension(boardDecision.get().getOriginalFilename()),new Long(boardDecision.get().getSize()+""), archiveId+"/"+checksum));

        checksum = transitionService.checksum(technicalReport.get().getOriginalFilename());
        storeRESTClient.storeFile(technicalReport.get().getBytes(), archiveId+"/", checksum);

        budget.setTechnicalReport(new Attachment(technicalReport.get().getOriginalFilename(), FileUtils.extension(technicalReport.get().getOriginalFilename()),new Long(technicalReport.get().getSize()+""), archiveId+"/"+checksum));
        budget.setCurrentStage(BudgetStages.Stage2.name());


        budget = super.add(budget,null);



        updateAcls(project, budget);

        return budget;

    }

    private void updateAcls(Project project, Budget budget){
        try{
            aclService.createAcl(new ObjectIdentityImpl(Budget.class, budget.getId()));
        }catch (AlreadyExistsException ex){
            logger.debug("Object identity already exists");
        }

        AclImpl acl = (AclImpl) aclService.readAclById(new ObjectIdentityImpl(Budget.class, budget.getId()));
        acl.insertAce(acl.getEntries().size(), ArcPermission.CANCEL, new PrincipalSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().toLowerCase()), true);

        acl.insertAce(acl.getEntries().size(), ArcPermission.CANCEL, new GrantedAuthoritySid("ROLE_ADMIN"), true);

        acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new GrantedAuthoritySid("ROLE_ADMIN"), true);
        acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()), true);
        acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(project.getScientificCoordinator().getEmail()), true);
        project.getScientificCoordinator().getDelegates().forEach(delegate -> acl.insertAce(acl.getEntries().size(), ArcPermission.READ, new PrincipalSid(delegate.getEmail()), true));

        acl.insertAce(acl.getEntries().size(), ArcPermission.EDIT, new PrincipalSid(project.getScientificCoordinator().getEmail()), true);
        project.getScientificCoordinator().getDelegates().forEach(delegate -> acl.insertAce(acl.getEntries().size(), ArcPermission.EDIT, new PrincipalSid(delegate.getEmail()), true));

        acl.insertAce(acl.getEntries().size(), ArcPermission.WRITE, new PrincipalSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()), true);

        acl.setOwner(new GrantedAuthoritySid(("ROLE_USER")));
        aclService.updateAcl(acl);
    }

    public int getMaxID() {

        return new JdbcTemplate(dataSource).query("select count(*)+1 AS next_id from budget_view where creation_date > current_date;", resultSet -> {
            if(resultSet.next())
                return resultSet.getInt("next_id");
            else
                return 1;
        });

    }

    public boolean alreadyExists(Project project, int year){

        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("year",year);
        in.addValue("project",project.getId());

        String query = "select CASE WHEN count(*)>0 THEN TRUE ELSE FALSE end AS alreadyExists from budget_view where year=:year AND project=:project AND status !='CANCELLED';";
        return new NamedParameterJdbcTemplate(dataSource).query(query, in, rs -> {
                rs.next();
                return rs.getBoolean("alreadyExists");
        });
    }

    @PreAuthorize("hasPermission(#budget,'READ')")
    public BudgetResponse getBudgetResponse(Budget budget){
        Project project = projectService.get(budget.getProjectId());
        Institute institute = instituteService.get(project.getInstituteId());


        BudgetResponse budgetResponse = new BudgetResponse();

        budgetResponse.setId(budget.getId());
        budgetResponse.setProjectId(project.getId());
        budgetResponse.setProjectAcronym(project.getAcronym());
        budgetResponse.setInstituteName(institute.getName());
        budgetResponse.setYear(budget.getYear());
        budgetResponse.setSubmittedBy(budget.getSubmittedBy());
        budgetResponse.setCreationDate(budget.getDate());
        budgetResponse.setBudgetStatus(budget.getBudgetStatus());
        budgetResponse.setStage(budget.getStage());
        budgetResponse.setRegularAmount(budget.getRegularAmount());
        budgetResponse.setContractAmount(budget.getContractAmount());
        budgetResponse.setTripAmount(budget.getTripAmount());
        budgetResponse.setServicesContractAmount(budget.getServicesContractAmount());
        budgetResponse.setComment(budget.getComment());
        budgetResponse.setStage2(budget.getStage2());
        budgetResponse.setStage4(budget.getStage4());
        budgetResponse.setStage5a(budget.getStage5a());
        budgetResponse.setStage6(budget.getStage6());
        budgetResponse.setBoardDecision(budget.getBoardDecision());
        budgetResponse.setTechnicalReport(budget.getTechnicalReport());
        budgetResponse.setCanEdit(hasPermission(budget.getId(),32));
        budgetResponse.setCanEditPrevious(hasPermission(budget.getId(),2));


        return budgetResponse;
    }

    private boolean hasPermission(String budgetId,int mask){
        //if mask==32 we are looking for EDIT right
        //if mask==2 we are looking for WRITE right
        String roles = "";
        for(GrantedAuthority grantedAuthority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()){
            roles = roles.concat(" or acl_sid.sid='"+grantedAuthority.getAuthority()+"'");
        }
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String aclEntriesQuery = "SELECT object_id_identity, canEdit FROM acl_object_identity INNER JOIN (select distinct acl_object_identity, CASE WHEN mask="+mask+" THEN true ELSE false END AS canEdit from acl_entry INNER JOIN acl_sid ON acl_sid.id=acl_entry.sid where acl_sid.sid='"+email+"' and acl_entry.mask="+mask+") as acl_entries ON acl_entries.acl_object_identity=acl_object_identity.id  INNER JOIN acl_class ON acl_class.id=acl_object_identity.object_id_class where acl_object_identity.object_id_identity='"+budgetId+"' AND acl_class.class='arc.athenarc.n4160.domain.Budget'";
        return new JdbcTemplate(dataSource).query(aclEntriesQuery , rs -> {

            if(rs.next())
                return rs.getBoolean("canEdit");
            else
                return false;
        });

    }


    public ResponseEntity<Object> upLoadFile(String archiveID,
                                             String stage, MultipartFile file) {
        String fileName = stage;
        if(Boolean.parseBoolean(storeRESTClient.fileExistsInArchive(archiveID,fileName).getResponse()))
            storeRESTClient.deleteFile(archiveID,fileName);

        try {
            storeRESTClient.storeFile(file.getBytes(),archiveID,fileName);
        } catch (IOException e) {
            logger.info(e);
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(archiveID+"/"+fileName,HttpStatus.OK);

    }

    @PreAuthorize("hasPermission(#budget,'READ')")
    public File downloadFile(File file, Budget budget, String url) {
        try {
            storeRESTClient.downloadFile(url, file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            logger.error("error downloading file", e);
        }
        return null;
    }

    @PreAuthorize("hasPermission(#budget,'WRITE')")
    public void deleteFile(Budget budget,String archiveId) {
        String[] splitted = archiveId.split("/");
        if(splitted.length!=2)
            throw new ServiceException("Bad archiveId format");
        storeRESTClient.deleteFile(splitted[0], splitted[1]);
    }

    public Attachment getAttachmentsFromBudget(Budget budget, String archiveId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class> stagesClasses = Arrays.stream(Budget.class.getDeclaredFields()).filter(p-> Stage.class.isAssignableFrom(p.getType())).flatMap(p -> Stream.of(p.getType())).collect(Collectors.toList());
        for(Class stageClass : stagesClasses) {
            if (Budget.class.getMethod("get" + stageClass.getSimpleName()).invoke(budget) != null) {
                Stage stage = (Stage) Budget.class.getMethod("get" + stageClass.getSimpleName()).invoke(budget);
                for(Attachment attachment : stage.getAttachments()){
                    if(attachment.getUrl().equals(archiveId))
                        return attachment;
                }
            }
        }
        if (budget.getTechnicalReport().getUrl().equals(archiveId))
            return budget.getTechnicalReport();

        if (budget.getBoardDecision().getUrl().equals(archiveId))
            return budget.getBoardDecision();

        return null;
    }

    public Paging<BudgetSummary> criteriaSearch(int from, int quantity,
                                                List<Budget.BudgetStatus> status, String searchField,
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

        String aclEntriesQuery = "select distinct on (d.object_id_identity) d.object_id_identity as id, d.requester as requester, d.year, d.project as project_id, d.creation_date, d.stage, d.status, d.canEdit, p.project_acronym, i.institute_id, i.institute_name, p.project_scientificcoordinator, p.project_operator, p.project_operator_delegate" +
                " from (" +
                "select o.object_id_identity, b.stage, b.status, b.budget_id,b.requester, b.year, b.project, b.creation_date, e.mask, CASE WHEN mask=32 and b.status in ('PENDING','UNDER_REVIEW') THEN true ELSE false END AS canEdit" +
                " from acl_entry e, acl_object_identity o, acl_sid s, budget_view b" +
                " where e.acl_object_identity = o.id and o.object_id_identity=b.budget_id and e.sid = s.id and s.sid in ('"+SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().toLowerCase()+"'"+(isAdmin ? ", 'ROLE_ADMIN'" : "")+" )" +
                ") d, project_view p, institute_view i, budget_view b" +
                " where b.project = p.project_id AND p.project_institute = i.institute_id " +
                " order by object_id_identity, canEdit desc";

        String viewQuery = "SELECT *, count(*) OVER() as totals FROM ("+aclEntriesQuery+") aclQ ";


        viewQuery+= " where "+(projectAcronym.isEmpty() ? "" : "project_acronym ILIKE :projectAcronym and ")+" "+(instituteName.isEmpty() ? "" : " ( institute_name ILIKE :institute OR institute_id ILIKE :institute) and ")+""+(requester.isEmpty() ? " " : " requester ILIKE :requester and ")+" status in ("+status.stream().map(p -> "'"+p.toString()+"'").collect(Collectors.joining(","))+") "+(canEdit ? "and canEdit=true " : "" )+"and stage in (:stages) "+(!searchField.isEmpty() ? "and (project_scientificcoordinator ILIKE :searchField or :searchField = any(project_operator) or :searchField = any(project_operator_delegate) or request_id=:searchField)" : "")+ (isMine ? " AND requester='"+SecurityContextHolder.getContext().getAuthentication().getPrincipal()+"'" : "")+" order by "+orderField+" "  +  orderType + " offset :offset limit :limit";
        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("searchField",searchField);
        in.addValue("projectAcronym","%"+projectAcronym+"%");
        in.addValue("institute", "%"+instituteName+"%");
        in.addValue("requester", "%"+requester+"%");
        in.addValue("stages",stages);
        in.addValue("offset",from);
        in.addValue("limit",quantity);

        logger.info(viewQuery);

        return new NamedParameterJdbcTemplate(dataSource).query(viewQuery, in, rs -> {
            List<BudgetSummary> results = new ArrayList<>();
            int totals = 0;
            while(rs.next()){
                logger.info(rs.getString("requester"));
                totals = rs.getInt("totals");
                BudgetSummary budgetSummary = new BudgetSummary();

                budgetSummary.setStage(rs.getString("stage"));
                budgetSummary.setId(rs.getString("id"));
                budgetSummary.setProjectId(rs.getString("project_id"));
                budgetSummary.setProjectAcronym(rs.getString("project_acronym"));
                budgetSummary.setYear(rs.getInt("year"));
                budgetSummary.setInstituteName(rs.getString("institute_name"));

                try {
                    User user = userService.getByField("user_email",rs.getString("requester"));
                    budgetSummary.setSubmittedByFullName(user.getFirstname() + " " + user.getLastname());
                } catch (Exception e) {
                   logger.warn(e);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                try {
                    budgetSummary.setCreationDate(sdf.parse(rs.getString("creation_date")).getTime());
                } catch (ParseException e) {
                    logger.warn("Failed to parse " + rs.getString("creation_date"));
                }

                budgetSummary.setBudgetStatus(Budget.BudgetStatus.fromValue(rs.getString("status")));
                budgetSummary.setStage(rs.getString("stage"));
                budgetSummary.setCanEdit(rs.getBoolean("canedit"));


                results.add(budgetSummary);
            }
            return new Paging<>(totals,from, from + results.size(), results, new ArrayList<>());
        });
    }


}
