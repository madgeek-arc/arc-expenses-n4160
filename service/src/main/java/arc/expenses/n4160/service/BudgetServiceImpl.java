package arc.expenses.n4160.service;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.acl.ArcPermission;
import arc.expenses.n4160.domain.*;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
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
        return new SimpleDateFormat("yyyyMMdd").format(new Date())+"-"+getMaxID();
    }



    public Budget add(String projectId,
                      int year,
                      Double regularAmount,
                      Double contractAmount,
                      Double tripAmount,
                      Double servicesContractAmount,
                      Optional<MultipartFile> boardDecision,
                      Optional<MultipartFile> technicalReport) throws Exception {

        Project project = projectService.get(projectId);

        User user = userService.getByField("user_email",(String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        if(user == null)
            throw new ServiceException("Logged in user not found in DB...(!)");

        if(project == null)
            throw new ServiceException("Project not found");

        if(!boardDecision.isPresent() || !technicalReport.isPresent())
            throw new ServiceException("Required attachments are missing");

        if(regularAmount < 0 || contractAmount < 0 || tripAmount < 0 || servicesContractAmount < 0)
            throw new ServiceException("Negative amounts are not accepted");

        List<String> pois = new ArrayList<>();
        pois.add(user.getEmail());

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

        String archiveId = requestService.createArchive();
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



}
