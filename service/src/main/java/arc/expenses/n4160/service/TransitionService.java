package arc.expenses.n4160.service;


import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.domain.BudgetStages;
import arc.expenses.n4160.domain.NormalStages;
import arc.expenses.n4160.domain.StageEvents;
import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import eu.openminted.store.restclient.StoreRESTClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("transitionService")
public class TransitionService {

    private static Logger logger = LogManager.getLogger(TransitionService.class);

    @Autowired
    private RequestApprovalServiceImpl requestApprovalService;

    @Autowired
    private AclService aclService;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private InstituteServiceImpl instituteService;

    @Autowired
    private OrganizationServiceImpl organizationService;

    @Autowired
    private RequestServiceImpl requestService;

    @Autowired
    private MailService mailService;

    @Autowired
    private StoreRESTClient storeRESTClient;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private BudgetServiceImpl budgetService;

    @Autowired
    private RequestPaymentServiceImpl requestPaymentService;

    public boolean checkContains(HttpServletRequest request, Class stageClass){
        List<String> requiredFields = Arrays.stream(stageClass.getDeclaredFields()).filter(p -> p.isAnnotationPresent(NotNull.class)).flatMap(p -> Stream.of(p.getName())).collect(Collectors.toList());
        Map<String, String[]> parameters = request.getParameterMap();
        for(String field: requiredFields){
            if(!parameters.containsKey(field)) {
                return false;
            }
        }

        return true;
    }


    public boolean checkContains(StateContext<NormalStages, StageEvents> context, Class stageClass){

        if(context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class) == null && context.getMessage().getHeaders().get("paymentObj", RequestPayment.class)==null) {
            context.getStateMachine().setStateMachineError(new ServiceException("Both request approval and payment objects are empty"));
            return false;
        }

        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        if(req == null) {
            context.getStateMachine().setStateMachineError(new ServiceException("Http request is empty"));
            return false;
        }
        List<String> requiredFields = Arrays.stream(stageClass.getDeclaredFields()).filter(p -> p.isAnnotationPresent(NotNull.class)).flatMap(p -> Stream.of(p.getName())).collect(Collectors.toList());
        Map<String, String[]> parameters = req.getParameterMap();
        for(String field: requiredFields){
            if(!parameters.containsKey(field)) {
                context.getStateMachine().setStateMachineError(new ServiceException(field + " is required"));
                return false;
            }
        }

        return true;
    }

    public boolean checkContainsBudget(StateContext<BudgetStages, StageEvents> context, Class stageClass){

        if(context.getMessage().getHeaders().get("budgetRequest", Budget.class) == null) {
            context.getStateMachine().setStateMachineError(new ServiceException("Budget request is null"));
            return false;
        }

        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        if(req == null) {
            context.getStateMachine().setStateMachineError(new ServiceException("Http request is empty"));
            return false;
        }
        List<String> requiredFields = Arrays.stream(stageClass.getDeclaredFields()).filter(p -> p.isAnnotationPresent(NotNull.class)).flatMap(p -> Stream.of(p.getName())).collect(Collectors.toList());
        Map<String, String[]> parameters = req.getParameterMap();
        for(String field: requiredFields){
            if(!parameters.containsKey(field)) {
                context.getStateMachine().setStateMachineError(new ServiceException(field + " is required"));
                return false;
            }
        }

        return true;
    }

    public void editApproval(StateContext<NormalStages, StageEvents> context, Stage stage, String stageString) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ResourceNotFoundException, InstantiationException, IOException {
        stage.setDate(new Date().toInstant().toEpochMilli());
        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

        Map<String, Class> keyValuePair = new HashMap<>();
        Arrays.stream(stage.getClass().getDeclaredFields()).forEach(p -> {
            keyValuePair.put(p.getName(),p.getType());
        });
        Map<String, String[]> parameters = req.getParameterMap();
        for(Map.Entry<String, Class> entry: keyValuePair.entrySet()){
            String field = entry.getKey();
            if(parameters.containsKey(field)) {
                    String upperCaseField = field.substring(0, 1).toUpperCase() + field.substring(1);
                    if(entry.getValue().isEnum())
                        stage.getClass().getMethod("set"+upperCaseField, entry.getValue()).invoke(stage, entry.getValue().getMethod("fromValue",String.class).invoke(entry.getValue(),req.getParameter(field)));
                    else
                        stage.getClass().getMethod("set"+upperCaseField, entry.getValue()).invoke(stage, entry.getValue().getConstructor(String.class).newInstance(req.getParameter(field)));


                    if(field.equalsIgnoreCase("amountInEuros")){
                        stage.getClass().getMethod("setFinalAmount", entry.getValue()).invoke(stage, entry.getValue().getConstructor(String.class).newInstance(req.getParameter(field)));
                    }
            }
        }

        String comment = Optional.ofNullable(req.getParameter("comment")).orElse(stage.getComment());

        Request request = requestService.get(requestApproval.getRequestId());
        stage.setComment(comment);
        stage.setAttachments(exportAttachments(request.getArchiveId(),(MultipartHttpServletRequest) req, stage));

        if(stage instanceof Stage1)
            requestApproval.setStage1((Stage1) stage);
        else if(stage instanceof Stage2)
            requestApproval.setStage2((Stage2) stage);
        else if(stage instanceof Stage3)
            requestApproval.setStage3((Stage3) stage);
        else if(stage instanceof Stage5b)
            requestApproval.setStage5b((Stage5b) stage);

        requestApprovalService.update(requestApproval,requestApproval.getId());

    }

    public void editPayment(StateContext<NormalStages, StageEvents> context, Stage stage, String stageString) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException, ResourceNotFoundException {
        stage.setDate(new Date().toInstant().toEpochMilli());
        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

        Map<String, Class> keyValuePair = new HashMap<>();
        Arrays.stream(stage.getClass().getDeclaredFields()).forEach(p -> {
            keyValuePair.put(p.getName(),p.getType());
        });
        Map<String, String[]> parameters = req.getParameterMap();
        for(Map.Entry<String, Class> entry: keyValuePair.entrySet()){
            String field = entry.getKey();
            if(parameters.containsKey(field)) {
                String upperCaseField = field.substring(0, 1).toUpperCase() + field.substring(1);
                if(entry.getValue().isEnum())
                    stage.getClass().getMethod("set"+upperCaseField, entry.getValue()).invoke(stage, entry.getValue().getMethod("fromValue",String.class).invoke(entry.getValue(),req.getParameter(field)));
                else
                    stage.getClass().getMethod("set"+upperCaseField, entry.getValue()).invoke(stage, entry.getValue().getConstructor(String.class).newInstance(req.getParameter(field)));


                if(field.equalsIgnoreCase("amountInEuros")){
                    stage.getClass().getMethod("setFinalAmount", entry.getValue()).invoke(stage, entry.getValue().getConstructor(String.class).newInstance(req.getParameter(field)));
                }
            }
        }
        String comment = Optional.ofNullable(req.getParameter("comment")).orElse(stage.getComment());

        Request request = requestService.get(requestPayment.getRequestId());

        stage.setComment(comment);
        stage.setAttachments(exportAttachments(request.getArchiveId(),(MultipartHttpServletRequest) req, stage));

        if(stage instanceof Stage7)
            requestPayment.setStage7((Stage7) stage);
        else if(stage instanceof Stage7a)
            requestPayment.setStage7a((Stage7a) stage);
        else if(stage instanceof Stage8)
            requestPayment.setStage8((Stage8) stage);
        else if(stage instanceof Stage9)
            requestPayment.setStage9((Stage9) stage);
        else if(stage instanceof Stage10)
            requestPayment.setStage10((Stage10) stage);
        else if(stage instanceof Stage11)
            requestPayment.setStage11((Stage11) stage);
        else if(stage instanceof Stage12)
            requestPayment.setStage12((Stage12) stage);
        else if(stage instanceof Stage13)
            requestPayment.setStage13((Stage13) stage);

        requestPaymentService.update(requestPayment,requestPayment.getId());

    }
    public void cancelRequestApproval(
            StateContext<NormalStages, StageEvents> context,
            String stage) throws Exception {

        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);

        Request request = requestService.get(requestApproval.getRequestId());
        request.setRequestStatus(Request.RequestStatus.CANCELLED);

        requestApproval.setStage(stage+"");
        requestApproval.setStatus(BaseInfo.Status.CANCELLED);
        requestApprovalService.update(requestApproval,requestApproval.getId());

        mailService.sendMail("CANCEL", request.getId(), projectService.get(request.getProjectId()).getAcronym(), requestApproval.getStage1().getRequestDate(), requestApproval.getStage1().getFinalAmount()+"", requestApproval.getStage1().getSubject(),false, requestApproval.getId(), request.getPois());

        requestService.update(request, request.getId());
        aclService.deleteAcl(new ObjectIdentityImpl(Request.class,request.getId()), true);
    }

    public void cancelRequestPayment(
            StateContext<NormalStages, StageEvents> context,
            String stage) throws Exception {
        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        Request request = requestService.get(requestPayment.getRequestId());

        Browsing<RequestPayment> payments = requestPaymentService.getPayments(request.getId(),null);
        requestPayment.setStage(stage+"");
        if(payments.getTotal()<=1) {
            boolean wholeRequest = Boolean.parseBoolean(Optional.ofNullable(req.getParameter("cancel_request")).orElse("false"));
            if (wholeRequest) {
                request.setRequestStatus(Request.RequestStatus.CANCELLED);

                RequestApproval requestApproval = requestApprovalService.getApproval(request.getId());
                requestApproval.setCurrentStage(NormalStages.CANCELLED.name());
                requestApproval.setStatus(BaseInfo.Status.CANCELLED);

                requestPayment.setStatus(BaseInfo.Status.CANCELLED);
                requestPayment.setCurrentStage(NormalStages.CANCELLED.name());

                requestApprovalService.update(requestApproval, requestApproval.getId());
                requestPaymentService.update(requestPayment,requestPayment.getId());
                requestService.update(request, request.getId());
            }else{
                requestPaymentService.createPayment(request);
                requestPaymentService.delete(requestPayment);
            }
        }else{
            requestPaymentService.delete(requestPayment);
        }
        aclService.removeEdit(requestPayment.getId(),RequestPayment.class);
        aclService.removeWrite(requestPayment.getId(),RequestPayment.class);
        RequestApproval requestApproval = requestApprovalService.getApproval(request.getId());
        mailService.sendMail("CANCEL", request.getId(), projectService.get(request.getProjectId()).getAcronym(), requestApproval.getStage1().getRequestDate(), requestApproval.getStage1().getFinalAmount()+"", requestApproval.getStage1().getSubject(),true, requestPayment.getId(), request.getPois());

    }

    public void editBudget(StateContext<BudgetStages, StageEvents> context, Stage stage, String stageString) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ResourceNotFoundException, InstantiationException, IOException {
        stage.setDate(new Date().toInstant().toEpochMilli());
        Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

        Map<String, Class> keyValuePair = new HashMap<>();
        Arrays.stream(stage.getClass().getDeclaredFields()).forEach(p -> {
            keyValuePair.put(p.getName(),p.getType());
        });
        Map<String, String[]> parameters = req.getParameterMap();
        for(Map.Entry<String, Class> entry: keyValuePair.entrySet()){
            String field = entry.getKey();
            if(parameters.containsKey(field)) {
                String upperCaseField = field.substring(0, 1).toUpperCase() + field.substring(1);
                if(entry.getValue().isEnum())
                    stage.getClass().getMethod("set"+upperCaseField, entry.getValue()).invoke(stage, entry.getValue().getMethod("fromValue",String.class).invoke(entry.getValue(),req.getParameter(field)));
                else
                    stage.getClass().getMethod("set"+upperCaseField, entry.getValue()).invoke(stage, entry.getValue().getConstructor(String.class).newInstance(req.getParameter(field)));
            }
        }

        String comment = Optional.ofNullable(req.getParameter("comment")).orElse(stage.getComment());
        stage.setComment(comment);
        stage.setAttachments(exportAttachments(budget.getArchiveId(),(MultipartHttpServletRequest) req, stage));


        if(stage instanceof Stage2)
            budget.setStage2((Stage2) stage);
        else if(stage instanceof Stage4)
            budget.setStage4((Stage4) stage);
        else if(stage instanceof Stage5a)
            budget.setStage5a((Stage5a) stage);
        else if(stage instanceof Stage6)
            budget.setStage6((Stage6) stage);

        budgetService.update(budget,budget.getId());
    }

    public void cancelBudget(
            StateContext<BudgetStages, StageEvents> context,
            String stage) throws Exception {
        Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

        budget.setBudgetStatus(Budget.BudgetStatus.CANCELLED);
        budget.setStage(stage+"");
        aclService.removeEdit(budget.getId(),Budget.class);
        aclService.removeWrite(budget.getId(),Budget.class);
        budgetService.update(budget,budget.getId());

        mailService.sendMail("CANCEL", budget.getId(), budget.getDate()+"", budget.getProjectId(), budget.getPois());

    }

    public void downgradeBudget(StateContext<BudgetStages, StageEvents> context, String fromStage, String toStage, Stage stage){
        Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        String comment = Optional.ofNullable(req.getParameter("comment")).orElse("");
        if(comment.isEmpty()) {
            context.getStateMachine().setStateMachineError(new ServiceException("We need a comment!"));
            throw new ServiceException("We need a comment!");
        }
        try {
            modifyRequestBudget(context, stage,toStage, Budget.BudgetStatus.UNDER_REVIEW);
            Project project = projectService.get(budget.getProjectId());
            updatingPermissions(fromStage,toStage,project,"DOWNGRADE", Budget.class,budget.getId(), stage.getDate()+"");
        } catch (Exception e) {
            logger.error("Error occurred on downgrade of budget " + budget.getId(),e);
            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
            throw new ServiceException(e.getMessage());
        }
    }

    public void rejectBudget(StateContext<BudgetStages, StageEvents> context, Stage stage, String rejectedAt) throws Exception {
        stage.setDate(new Date().toInstant().toEpochMilli());
        modifyRequestBudget(context, stage,rejectedAt, Budget.BudgetStatus.PENDING);
    }

    public void approveBudget(StateContext<BudgetStages, StageEvents> context, String fromStage, String toStage, Stage stage) throws Exception {
        Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        modifyRequestBudget(context, stage, toStage, (fromStage.equals("6") && toStage.equals("6") ? Budget.BudgetStatus.ACCEPTED : Budget.BudgetStatus.PENDING));
        Project project = projectService.get(budget.getProjectId());
        if(fromStage.equals("6") && toStage.equals("6")) {
            aclService.removeEdit(budget.getId(),Budget.class);
            aclService.removeWrite(budget.getId(),Budget.class);
            ArrayList<Sid> principals = new ArrayList<>();
            principals.add(new GrantedAuthoritySid("ROLE_ADMIN"));
            project.getOperator().forEach( operator -> {
                principals.add(new PrincipalSid(operator.getEmail()));
                operator.getDelegates().forEach(delegate -> principals.add(new PrincipalSid(delegate.getEmail())));
            });
            aclService.addWrite(principals,budget.getId(),Budget.class);
        }else
            updatingPermissions(fromStage,toStage, project, "APPROVE", Budget.class,budget.getId(), stage.getDate()+"");

    }

    public void modifyRequestBudget(
            StateContext<BudgetStages, StageEvents> context,
            Stage stage,
            String stageString,
            Budget.BudgetStatus status) throws Exception {

        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);

        String comment = Optional.ofNullable(req.getParameter("comment")).orElse("");


        stage.setAttachments(exportAttachments(budget.getArchiveId(),(MultipartHttpServletRequest) req, stage));
        stage.setComment(comment);
        try {
            User user = userService.getByField("user_email",(String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            stage.setUser(user);
        } catch (Exception e) {
            context.getStateMachine().setStateMachineError(new ServiceException("User not found"));
            throw new ServiceException("User not found");
        }

        if(stage instanceof Stage2)
            budget.setStage2((Stage2) stage);
        else if(stage instanceof Stage4)
            budget.setStage4((Stage4) stage);
        else if(stage instanceof Stage5a)
            budget.setStage5a((Stage5a) stage);
        else if(stage instanceof Stage6)
            budget.setStage6((Stage6) stage);

        budget.setStage(stageString);
        budget.setBudgetStatus(status);
        if(status== Budget.BudgetStatus.ACCEPTED) {
            Project project = projectService.get(budget.getProjectId());
            updatingPermissions("6","6",project, "APPROVE", Budget.class,budget.getId(), stage.getDate()+"");
            aclService.removeEdit(budget.getId(),Budget.class);
            budget.setCurrentStage(BudgetStages.FINISHED.name());
        }
        budgetService.update(budget,budget.getId());

        if(status == Budget.BudgetStatus.REJECTED){
            mailService.sendMail("REJECT", budget.getId(), budget.getDate()+"",budget.getProjectId(), budget.getPois());
        }
    }



    public void modifyRequestApproval(
            StateContext<NormalStages, StageEvents> context,
            Stage stage,
            String stageString,
            String fromStage,
            BaseInfo.Status status) throws Exception {

        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
        Request request = requestService.get(requestApproval.getRequestId());

        String comment = Optional.ofNullable(req.getParameter("comment")).orElse("");


        stage.setAttachments(exportAttachments(request.getArchiveId(),(MultipartHttpServletRequest) req, stage));
        stage.setComment(comment);
        try {
            User user = userService.getByField("user_email",(String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            stage.setUser(user);
        } catch (Exception e) {
            context.getStateMachine().setStateMachineError(new ServiceException("User not found"));
            throw new ServiceException("User not found");
        }

        if(stage instanceof Stage1)
            requestApproval.setStage1((Stage1) stage);
        else if(stage instanceof Stage2)
            requestApproval.setStage2((Stage2) stage);
        else if(stage instanceof Stage3)
            requestApproval.setStage3((Stage3) stage);
        else if(stage instanceof Stage5b)
            requestApproval.setStage5b((Stage5b) stage);

        requestApproval.setStage((stageString.equals("FINISHED") ? fromStage : stageString));
        requestApproval.setStatus(status);
        if(status== BaseInfo.Status.ACCEPTED) {
            if(request.getType() == Request.Type.CONTRACT){
                request.setRequestStatus(Request.RequestStatus.ACCEPTED);
                requestApproval.setCurrentStage(NormalStages.FINISHED.name());
            }else{
                requestPaymentService.createPayment(request);
            }
            Project project = projectService.get(request.getProjectId());
            updatingPermissions(fromStage,"FINISHED",project, "APPROVE", RequestApproval.class,requestApproval.getId(), stage.getDate()+"");
            aclService.removeEdit(requestApproval.getId(),RequestApproval.class);
            requestService.update(request,request.getId());
        }
        requestApprovalService.update(requestApproval,requestApproval.getId());
        if(status == BaseInfo.Status.REJECTED){
            request.setRequestStatus(Request.RequestStatus.REJECTED);
            requestService.update(request,request.getId());
//            aclService.deleteAcl(new ObjectIdentityImpl(Request.class,request.getId()), true);
            mailService.sendMail("REJECT", request.getId(), projectService.get(request.getProjectId()).getAcronym(), requestApproval.getStage1().getRequestDate(), requestApproval.getStage1().getFinalAmount()+"", requestApproval.getStage1().getSubject(),false, requestApproval.getId(), request.getPois());
        }
    }


    public void modifyRequestPayment(
            StateContext<NormalStages, StageEvents> context,
            Stage stage,
            String stageString,
            BaseInfo.Status status) throws Exception {
        stage.setDate(new Date().toInstant().toEpochMilli());
        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
        Request request = requestService.get(requestPayment.getRequestId());
        String comment = Optional.ofNullable(req.getParameter("comment")).orElse("");

        stage.setAttachments(exportAttachments(request.getArchiveId(),(MultipartHttpServletRequest) req, stage));
        stage.setComment(comment);
        try {
            User user = userService.getByField("user_email",(String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            stage.setUser(user);
        } catch (Exception e) {
            context.getStateMachine().setStateMachineError(new ServiceException("User not found"));
            throw new ServiceException("User not found");
        }

        if(stage instanceof Stage7)
            requestPayment.setStage7((Stage7) stage);
        else if(stage instanceof Stage7a)
            requestPayment.setStage7a((Stage7a) stage);
        else if(stage instanceof Stage8)
            requestPayment.setStage8((Stage8) stage);
        else if(stage instanceof Stage9)
            requestPayment.setStage9((Stage9) stage);
        else if(stage instanceof Stage10)
            requestPayment.setStage10((Stage10) stage);
        else if(stage instanceof Stage11)
            requestPayment.setStage11((Stage11) stage);
        else if(stage instanceof Stage12)
            requestPayment.setStage12((Stage12) stage);
        else if(stage instanceof Stage13)
            requestPayment.setStage13((Stage13) stage);

        requestPayment.setStage(stageString);
        requestPayment.setStatus(status);
        requestPaymentService.update(requestPayment,requestPayment.getId());

        if(status == BaseInfo.Status.REJECTED){
            if(request.getType() != Request.Type.SERVICES_CONTRACT){
                request.setRequestStatus(Request.RequestStatus.REJECTED);
                requestService.update(request,request.getId());
                RequestApproval requestApproval = requestApprovalService.getApproval(request.getId());
                mailService.sendMail("REJECT", request.getId(), projectService.get(request.getProjectId()).getAcronym(), requestApproval.getStage1().getRequestDate(), requestApproval.getStage1().getFinalAmount()+"", requestApproval.getStage1().getSubject(),true, requestPayment.getId(), request.getPois());
            }

        }
    }

    public void approveApproval(StateContext<NormalStages, StageEvents> context, String fromStage, String toStage, Stage stage) throws Exception {
        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        Request request = requestService.get(requestApproval.getRequestId());

        modifyRequestApproval(context, stage, toStage, fromStage,(toStage.equals("FINISHED") ? BaseInfo.Status.ACCEPTED : BaseInfo.Status.PENDING));

        if(toStage.equalsIgnoreCase("5b")){
            Project project = projectService.get(request.getProjectId());
            Institute institute = instituteService.get(project.getInstituteId());
            Organization organization = organizationService.get(institute.getOrganizationId());

            request.setDiataktis(institute.getDiataktis());

            if(request.getUser().getEmail().equalsIgnoreCase(request.getDiataktis().getEmail()) || (request.getOnBehalfOf() != null && request.getOnBehalfOf().getEmail().equalsIgnoreCase(request.getDiataktis().getEmail()))){
                if(request.getDiataktis().getEmail().equalsIgnoreCase(organization.getDirector().getEmail()))
                    request.setDiataktis(organization.getViceDirector());
                else
                    request.setDiataktis(organization.getDirector());
            }


            requestService.update(request,request.getId());
        }
        Project project = projectService.get(request.getProjectId());
        updatingPermissions(fromStage,toStage, project, "APPROVE", RequestApproval.class,requestApproval.getId(), stage.getDate()+"");

    }

    public void approvePayment(StateContext<NormalStages, StageEvents> context, String fromStage, String toStage, Stage stage) throws Exception {
        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        Request request = requestService.get(requestPayment.getRequestId());
        BaseInfo.Status status = (toStage.equalsIgnoreCase("13") && fromStage.equalsIgnoreCase("13") ? BaseInfo.Status.ACCEPTED : BaseInfo.Status.PENDING);
        if(toStage.equalsIgnoreCase("13") && fromStage.equalsIgnoreCase("13")){ // that's the signal for a finished payment
            Browsing<RequestPayment> payments = requestPaymentService.getPayments(request.getId(),null);
            if(payments.getResults().size()>=request.getPaymentCycles()){ //if we have reached the max of payment cycles then request should be automatically move to FINISHED state
                RequestApproval requestApproval = requestApprovalService.getApproval(requestPayment.getRequestId());
                request.setRequestStatus(Request.RequestStatus.ACCEPTED);
                requestApproval.setCurrentStage(NormalStages.FINISHED.name());
                requestService.update(request,request.getId());
                requestApprovalService.update(requestApproval,requestApproval.getId());
            }
        }

        modifyRequestPayment(context, stage, toStage, status);
        Project project = projectService.get(request.getProjectId());
        updatingPermissions(fromStage,toStage, project, "APPROVE",RequestPayment.class,requestPayment.getId(),stage.getDate()+"");
        updatingPermissions(fromStage,toStage,project,"",RequestApproval.class,requestApprovalService.getApproval(request.getId()).getId(), stage.getDate()+"");

    }

    public void rejectApproval(StateContext<NormalStages, StageEvents> context, Stage stage, String rejectedAt) throws Exception {
        stage.setDate(new Date().toInstant().toEpochMilli());
        modifyRequestApproval(context, stage,rejectedAt, "",BaseInfo.Status.REJECTED);
    }

    public void rejectPayment(StateContext<NormalStages, StageEvents> context,Stage stage, String rejectedAt) throws Exception {
        stage.setDate(new Date().toInstant().toEpochMilli());
        modifyRequestPayment(context, stage,rejectedAt, BaseInfo.Status.REJECTED);
    }


    public void downgradeApproval(StateContext<NormalStages, StageEvents> context, String fromStage, String toStage, Stage stage){
        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
        MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        String comment = Optional.ofNullable(req.getParameter("comment")).orElse("");
        stage.setDate(new Date().toInstant().toEpochMilli());
        if(comment.isEmpty()) {
            context.getStateMachine().setStateMachineError(new ServiceException("We need a comment!"));
            throw new ServiceException("We need a comment!");
        }
        try {
            Request request = requestService.get(requestApproval.getRequestId());
            modifyRequestApproval(context, stage,toStage,"", BaseInfo.Status.UNDER_REVIEW);
            Project project = projectService.get(request.getProjectId());
            updatingPermissions(fromStage,toStage,project,"Downgrade",RequestApproval.class,requestApproval.getId(), stage.getDate()+"");
        } catch (Exception e) {
            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
            throw new ServiceException(e.getMessage());
        }
    }

    public void downgradePayment(StateContext<NormalStages, StageEvents> context, String fromStage, String toStage, Stage stage){
        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
        stage.setDate(new Date().toInstant().toEpochMilli());
        MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
        String comment = Optional.ofNullable(req.getParameter("comment")).orElse("");
        if(comment.isEmpty()) {
            context.getStateMachine().setStateMachineError(new ServiceException("We need a comment!"));
            throw new ServiceException("We need a comment!");
        }
        try {
            modifyRequestPayment(context, stage,toStage, BaseInfo.Status.UNDER_REVIEW);
            Project project = projectService.get(requestService.get(requestPayment.getRequestId()).getProjectId());
            updatingPermissions(fromStage,toStage,project,"Downgrade", RequestPayment.class,requestPayment.getId(), stage.getDate()+"");
        } catch (Exception e) {
            logger.error("Error occurred on approval of payment " + requestPayment.getId(),e);
            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
            throw new ServiceException(e.getMessage());
        }
    }

    public void updatingPermissions(String from, String to, Project project, String mailType, Class persistentClass, String id, String dateToSend) throws Exception {
        List<Sid> revokeEditAccess = new ArrayList<>();
        List<Sid> grantAccess = new ArrayList<>();
        List<Sid> grantWrite = new ArrayList<>();
        Institute institute = instituteService.get(project.getInstituteId());
        Organization organization = organizationService.get(institute.getOrganizationId());
        String requester = "";
        String requestId = "";
        Request.Type requestType = null;
        PersonOfInterest diataktis = null;
        PersonOfInterest onBehalf = null;
        Request request = null;
        Budget budget = null;
        if(persistentClass.equals(RequestApproval.class)){
            request = requestService.get(requestApprovalService.get(id).getRequestId());
            requester = request.getUser().getEmail();
            diataktis = request.getDiataktis();
            requestType = request.getType();
            requestId = request.getId();
            onBehalf = request.getOnBehalfOf();
        }else if(persistentClass.equals(RequestPayment.class)) {
            request = requestService.get(requestPaymentService.get(id).getRequestId());
            requester = request.getUser().getEmail();
            diataktis = request.getDiataktis();
            requestType = request.getType();
            requestId = request.getId();
            onBehalf = request.getOnBehalfOf();
        }else if(persistentClass.equals(Budget.class)){
            budget = budgetService.get(id);
            requester = budget.getSubmittedBy().getEmail();
            diataktis = institute.getDiataktis();
        }

        switch (from){
            case "1":
                revokeEditAccess.add(new PrincipalSid(requester));
                break;
            case "2":
                revokeEditAccess.add(new PrincipalSid(project.getScientificCoordinator().getEmail()));
                project.getScientificCoordinator().getDelegates().forEach(person -> revokeEditAccess.add(new PrincipalSid(person.getEmail())));
                break;
            case "3":
                project.getOperator().forEach(entry -> {
                    revokeEditAccess.add(new PrincipalSid(entry.getEmail()));
                    entry.getDelegates().forEach(person -> {
                        revokeEditAccess.add(new PrincipalSid(person.getEmail()));
                    });
                });
                break;
            case "9":
            case "4":
                revokeEditAccess.add(new PrincipalSid(organization.getPoy().getEmail()));
                organization.getPoy().getDelegates().forEach(delegate -> {
                    revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            case "10":
            case "5a":
                revokeEditAccess.add(new PrincipalSid(diataktis.getEmail()));
                diataktis.getDelegates().forEach( delegate -> {
                    revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            case "5b":
                revokeEditAccess.add(new PrincipalSid(organization.getDioikitikoSumvoulio().getEmail()));
                organization.getDioikitikoSumvoulio().getDelegates().forEach(delegate -> revokeEditAccess.add(new PrincipalSid(delegate.getEmail())));
                break;
            case "11":
            case "6":
                revokeEditAccess.add(new PrincipalSid(institute.getDiaugeia().getEmail()));
                institute.getDiaugeia().getDelegates().forEach(delegate -> revokeEditAccess.add(new PrincipalSid(delegate.getEmail())));
                break;
            case "7":
                revokeEditAccess.add(new PrincipalSid(requester));

                if(requestType == Request.Type.TRIP) {
                    revokeEditAccess.add(new PrincipalSid(institute.getTravelManager().getEmail()));
                    institute.getTravelManager().getDelegates().forEach(delegate -> {
                        revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                }else{
                    revokeEditAccess.add(new PrincipalSid(institute.getSuppliesOffice().getEmail()));
                    institute.getSuppliesOffice().getDelegates().forEach(delegate -> {
                        revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                }
                break;
            case "7a":
                project.getOperator().forEach( operator -> {
                    revokeEditAccess.add(new PrincipalSid(operator.getEmail()));
                    operator.getDelegates().forEach(delegate -> {
                        revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                });
                break;
            case "8":
                organization.getInspectionTeam().forEach(inspector -> {
                    revokeEditAccess.add(new PrincipalSid(inspector.getEmail()));
                    inspector.getDelegates().forEach(delegate -> {
                        revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                });
                break;
            case "12":
                revokeEditAccess.add(new PrincipalSid(institute.getAccountingRegistration().getEmail()));
                institute.getAccountingRegistration().getDelegates().forEach(delegate -> {
                    revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            case "13":
                revokeEditAccess.add(new PrincipalSid(institute.getAccountingPayment().getEmail()));
                institute.getAccountingPayment().getDelegates().forEach(delegate -> {
                    revokeEditAccess.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            default:
                break;

        }

        switch (to){
            case "1":
                grantAccess.add(new PrincipalSid(requester));
                break;
            case "2":
                grantAccess.add(new PrincipalSid(project.getScientificCoordinator().getEmail()));
                project.getScientificCoordinator().getDelegates().forEach(person -> grantAccess.add(new PrincipalSid(person.getEmail())));

                grantWrite.add(new PrincipalSid(requester));
                break;
            case "3":
                project.getOperator().forEach(entry -> {
                    grantAccess.add(new PrincipalSid(entry.getEmail()));
                    entry.getDelegates().forEach(person -> {
                        grantAccess.add(new PrincipalSid(person.getEmail()));
                    });
                });

                grantWrite.add(new PrincipalSid(project.getScientificCoordinator().getEmail()));
                project.getScientificCoordinator().getDelegates().forEach(person -> grantWrite.add(new PrincipalSid(person.getEmail())));
                break;
            case "4":
                grantAccess.add(new PrincipalSid(organization.getPoy().getEmail()));
                organization.getPoy().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });

                project.getOperator().forEach(entry -> {
                    grantWrite.add(new PrincipalSid(entry.getEmail()));
                    entry.getDelegates().forEach(person -> {
                        grantWrite.add(new PrincipalSid(person.getEmail()));
                    });
                });

                break;

            case "10":
            case "5a":
                grantAccess.add(new PrincipalSid(diataktis.getEmail()));
                String finalRequester = requester;
                diataktis.getDelegates().forEach(delegate -> {
                    if(!finalRequester.equalsIgnoreCase(delegate.getEmail()))
                        grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });

                grantWrite.add(new PrincipalSid(organization.getPoy().getEmail()));
                organization.getPoy().getDelegates().forEach(delegate -> {
                    grantWrite.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            case "5b":
                grantAccess.add(new PrincipalSid(organization.getDioikitikoSumvoulio().getEmail()));
                organization.getDioikitikoSumvoulio().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });

                project.getOperator().forEach(operator -> {
                    grantWrite.add(new PrincipalSid(operator.getEmail()));
                    operator.getDelegates().forEach(delegate -> {
                        grantWrite.add(new PrincipalSid(operator.getEmail()));
                    });
                });
                break;
            case "6":
                grantAccess.add(new PrincipalSid(institute.getDiaugeia().getEmail()));
                institute.getDiaugeia().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });
                RequestApproval requestApproval = requestApprovalService.getApproval(requestId);
                if(requestApproval ==null) {
                    grantWrite.add(new PrincipalSid(institute.getDiataktis().getEmail()));
                    institute.getDiataktis().getDelegates().forEach(delegate -> {
                        grantWrite.add(new PrincipalSid(delegate.getEmail()));
                    });
                }
                break;
            case "7":
                grantAccess.add(new PrincipalSid(requester));
                if(requestType == Request.Type.TRIP) {
                    grantAccess.add(new PrincipalSid(institute.getTravelManager().getEmail()));
                    institute.getTravelManager().getDelegates().forEach(delegate -> {
                        grantAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                }else{
                    grantAccess.add(new PrincipalSid(institute.getSuppliesOffice().getEmail()));
                    institute.getSuppliesOffice().getDelegates().forEach(delegate -> {
                        grantAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                }
                grantWrite.add(new PrincipalSid(institute.getDiaugeia().getEmail()));
                institute.getDiaugeia().getDelegates().forEach(delegate -> {
                    grantWrite.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            case "7a":
                project.getOperator().forEach( operator -> {
                    grantAccess.add(new PrincipalSid(operator.getEmail()));
                    operator.getDelegates().forEach(delegate -> {
                        grantAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                });

                grantWrite.add(new PrincipalSid(requester));

                if(requestType == Request.Type.TRIP) {
                    grantWrite.add(new PrincipalSid(institute.getTravelManager().getEmail()));
                    institute.getTravelManager().getDelegates().forEach(delegate -> {
                        grantWrite.add(new PrincipalSid(delegate.getEmail()));
                    });
                }else{
                    grantWrite.add(new PrincipalSid(institute.getSuppliesOffice().getEmail()));
                    institute.getSuppliesOffice().getDelegates().forEach(delegate -> {
                        grantWrite.add(new PrincipalSid(delegate.getEmail()));
                    });
                }

                break;
            case "8":
                String finalRequester3 = requester;
                PersonOfInterest finalOnBehalf = onBehalf;
                organization.getInspectionTeam().forEach(inspector -> {
                    if(!finalRequester3.equalsIgnoreCase(inspector.getEmail()) && (finalOnBehalf ==null || !finalOnBehalf.getEmail().equalsIgnoreCase(inspector.getEmail())))
                        grantAccess.add(new PrincipalSid(inspector.getEmail()));

                    inspector.getDelegates().forEach(delegate -> {
                        if(!finalRequester3.equalsIgnoreCase(delegate.getEmail()) && (finalOnBehalf==null || finalOnBehalf.getEmail().equalsIgnoreCase(delegate.getEmail())))
                            grantAccess.add(new PrincipalSid(delegate.getEmail()));
                    });
                });

                project.getOperator().forEach( operator -> {
                    grantWrite.add(new PrincipalSid(operator.getEmail()));
                    operator.getDelegates().forEach(delegate -> {
                        grantWrite.add(new PrincipalSid(delegate.getEmail()));
                    });
                });

                break;
            case "9":
                grantAccess.add(new PrincipalSid(organization.getPoy().getEmail()));
                organization.getPoy().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });

                String finalRequester4 = requester;
                PersonOfInterest finalOnBehalf1 = onBehalf;
                organization.getInspectionTeam().forEach(entry -> {
                    if(!finalRequester4.equalsIgnoreCase(entry.getEmail()) && (finalOnBehalf1 ==null || !finalOnBehalf1.getEmail().equalsIgnoreCase(entry.getEmail())))
                        grantWrite.add(new PrincipalSid(entry.getEmail()));
                    entry.getDelegates().forEach(person -> {
                        if(!finalRequester4.equalsIgnoreCase(person.getEmail()) && (finalOnBehalf1==null || !finalOnBehalf1.getEmail().equalsIgnoreCase(person.getEmail())))
                            grantWrite.add(new PrincipalSid(person.getEmail()));
                    });
                });
                break;
            case "11":
                grantAccess.add(new PrincipalSid(institute.getDiaugeia().getEmail()));
                institute.getDiaugeia().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });

                grantWrite.add(new PrincipalSid(diataktis.getEmail()));
                String finalRequester5 = requester;
                PersonOfInterest finalOnBehalf2 = onBehalf;
                diataktis.getDelegates().forEach(delegate -> {
                    if(!finalRequester5.equalsIgnoreCase(delegate.getEmail())&& (finalOnBehalf2 ==null || !finalOnBehalf2.getEmail().equalsIgnoreCase(delegate.getEmail())))
                        grantWrite.add(new PrincipalSid(delegate.getEmail()));
                });

                break;
            case "12":
                grantAccess.add(new PrincipalSid(institute.getAccountingRegistration().getEmail()));
                institute.getAccountingRegistration().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });


                grantWrite.add(new PrincipalSid(institute.getDiaugeia().getEmail()));
                institute.getDiaugeia().getDelegates().forEach(delegate -> {
                    grantWrite.add(new PrincipalSid(delegate.getEmail()));
                });

                break;
            case "13":
                grantAccess.add(new PrincipalSid(institute.getAccountingPayment().getEmail()));
                institute.getAccountingPayment().getDelegates().forEach(delegate -> {
                    grantAccess.add(new PrincipalSid(delegate.getEmail()));
                });


                grantWrite.add(new PrincipalSid(institute.getAccountingRegistration().getEmail()));
                institute.getAccountingRegistration().getDelegates().forEach(delegate -> {
                    grantWrite.add(new PrincipalSid(delegate.getEmail()));
                });
                break;
            case "FINISHED":
                requestApproval = requestApprovalService.getApproval(requestId);
                if(requestApproval!=null && requestApproval.getStage5b() != null){
                    grantWrite.add(new PrincipalSid(organization.getDioikitikoSumvoulio().getEmail()));
                    organization.getDioikitikoSumvoulio().getDelegates().forEach( delegate -> {
                        grantWrite.add(new PrincipalSid(delegate.getEmail()));
                    });
                }else if(requestApproval != null && requestApproval.getStage5b()==null){
                    project.getOperator().forEach(operator -> {
                        grantWrite.add(new PrincipalSid(operator.getEmail()));
                        operator.getDelegates().forEach(delegate -> grantWrite.add(new PrincipalSid(delegate.getEmail())));
                    });
                }
                break;
            default:
                break;
        }

        aclService.updateAclEntries(revokeEditAccess,grantAccess,id, persistentClass);
        aclService.removeWrite(id,persistentClass);
        aclService.addWrite(grantWrite,id,persistentClass);

        if(!mailType.isEmpty()) {
            if (persistentClass == RequestPayment.class || persistentClass == RequestApproval.class) {
                boolean isPayment = false;
                if (persistentClass == RequestPayment.class)
                    isPayment = true;
                mailService.sendMail(mailType, request.getId(), project.getAcronym(), dateToSend, request.getFinalAmount() + "", requestApprovalService.getApproval(request.getId()).getStage1().getSubject(), isPayment, id, grantAccess.stream().map(entry -> ((PrincipalSid) entry).getPrincipal()).collect(Collectors.toList()));
            }else{

                mailService.sendMail(mailType, budget.getId(), dateToSend, budget.getProjectId(), grantAccess.stream().map(entry -> ((PrincipalSid) entry).getPrincipal()).collect(Collectors.toList()));
            }
        }

        List<String> pois = new ArrayList<>();
        if(persistentClass == RequestPayment.class || persistentClass == RequestApproval.class) {
            pois = request.getPois();
            List<String> finalPois = pois;
            grantAccess.forEach(granted -> {
                if(!finalPois.contains(((PrincipalSid) granted).getPrincipal())){
                    finalPois.add(((PrincipalSid) granted).getPrincipal());
                }
            });
            request.setPois(finalPois);
            requestService.update(request,request.getId());
        }else if(persistentClass == Budget.class) {
            pois = budget.getPois();
            List<String> finalPois = pois;
            grantAccess.forEach(granted -> {
                if(!finalPois.contains(((PrincipalSid) granted).getPrincipal())){
                    finalPois.add(((PrincipalSid) granted).getPrincipal());
                }
            });
            budget.setPois(pois);
            budgetService.update(budget,budget.getId());
        }

    }


    public String checksum(String filename){
        MessageDigest md = null;
        try {
            String extension = FileUtils.extension(filename);
            filename = filename + new Date().getTime();
            md = MessageDigest.getInstance("MD5");
            md.update(filename.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toLowerCase() + "."+extension;
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Failed to generate MD5 hash of filename:"+filename);
            return filename;
        }

    }

    private List<Attachment> exportAttachments(String archiveId,MultipartHttpServletRequest multiPartRequest, Stage stage) throws IOException {
        List<Attachment> attachments = Optional.ofNullable(stage.getAttachments()).orElse(new ArrayList<>());
        List<String> removed = Arrays.asList(Optional.ofNullable(multiPartRequest.getParameterValues("removed")).orElse(new String[]{}));
        if(removed.size()>0){
            for(String toBeRemoved : removed){
                String[] splitted = toBeRemoved.split("/");
                storeRESTClient.deleteFile(splitted[0],splitted[1]);
            }
            attachments = attachments.stream().filter(attachment -> {
                for(String remove : removed){
                    if(remove.equalsIgnoreCase(attachment.getUrl()))
                        return false;
                }
                return true;
            }).collect(Collectors.toList());
        }
        for(MultipartFile file : multiPartRequest.getFiles("attachments")){
            String checksum = checksum(file.getOriginalFilename());
            storeRESTClient.storeFile(file.getBytes(), archiveId+"/", checksum);
            attachments.add(new Attachment(file.getOriginalFilename(), FileUtils.extension(file.getOriginalFilename()),new Long(file.getSize()+""), archiveId+"/"+checksum));
        }
        return attachments;
    }

}
