package arc.expenses.n4160.config.stateMachine;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.domain.BudgetStages;
import arc.expenses.n4160.domain.NormalStages;
import arc.expenses.n4160.domain.StageEvents;
import arc.expenses.n4160.service.BudgetServiceImpl;
import arc.expenses.n4160.service.ProjectServiceImpl;
import arc.expenses.n4160.service.TransitionService;
import eu.openminted.registry.core.service.ServiceException;
import eu.openminted.store.restclient.StoreRESTClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Configuration
@EnableStateMachineFactory
public class BudgetStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<BudgetStages, StageEvents> {

    private static Logger logger = LogManager.getLogger(BudgetStateMachineConfiguration.class);

    @Autowired
    private TransitionService transitionService;

    @Autowired
    private BudgetServiceImpl budgetService;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private StoreRESTClient storeRESTClient;

    @Override
    public void configure(StateMachineStateConfigurer<BudgetStages, StageEvents> states) throws Exception {
        states.withStates()
                .initial(BudgetStages.Stage1)
                .end(BudgetStages.FINISHED)
                .end(BudgetStages.REJECTED)
                .end(BudgetStages.CANCELLED)
                .states(EnumSet.allOf(BudgetStages.class))
            ;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<BudgetStages, StageEvents> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(loggingListener());
    }

    public StateMachineListener<BudgetStages, StageEvents> loggingListener() {
        return new StateMachineListenerAdapter<BudgetStages, StageEvents>() {
            @Override
            public void stateChanged(State<BudgetStages, StageEvents> from, State<BudgetStages, StageEvents> to) {

            }

            @Override
            public void stateMachineError(StateMachine<BudgetStages, StageEvents> stateMachine, Exception exception) {
                logger.info("Exception received from budget machine");
                stateMachine.getExtendedState().getVariables().put("error",exception.getMessage());
            }

            @Override
            public void eventNotAccepted(Message<StageEvents> event) {
                logger.error("Event, on budget machine, not accepted: {}", event.getPayload());
            }
        };
    }

    @Override
    @DependsOn("factory")
    public void configure(StateMachineTransitionConfigurer<BudgetStages, StageEvents> transitions) throws Exception {

        transitions.withExternal()
                .source(BudgetStages.Stage1)
                .target(BudgetStages.Stage2)
                .event(StageEvents.APPROVE)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage1.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                    try {
                        budget.setContractAmount(Double.parseDouble(req.getParameter("contractAmount")));
                        budget.setRegularAmount(Double.parseDouble(req.getParameter("regularAmount")));
                        budget.setTripAmount(Double.parseDouble(req.getParameter("tripAmount")));
                        budget.setServicesContractAmount(Double.parseDouble(req.getParameter("servicesContractAmount")));

                        if(req.getParameter("comment") != null)
                            budget.setComment(req.getParameter("comment"));

                        if(req.getFile("technicalReport") !=null) {

                            MultipartFile technicalReport = req.getFile("technicalReport");
                            storeRESTClient.storeFile(technicalReport.getBytes(), budget.getArchiveId()+"/", transitionService.checksum(technicalReport.getOriginalFilename()));
                            budget.setTechnicalReport(new Attachment(technicalReport.getOriginalFilename(), FileUtils.extension(technicalReport.getOriginalFilename()),new Long(technicalReport.getSize()+""), budget.getArchiveId()+"/"+ transitionService.checksum(technicalReport.getOriginalFilename())));
                        }

                        if(req.getFile("boardDecision") !=null) {

                            MultipartFile boardDecision = req.getFile("boardDecision");
                            storeRESTClient.storeFile(boardDecision.getBytes(), budget.getArchiveId()+"/", transitionService.checksum(boardDecision.getOriginalFilename()));
                            budget.setBoardDecision(new Attachment(boardDecision.getOriginalFilename(), FileUtils.extension(boardDecision.getOriginalFilename()),new Long(boardDecision.getSize()+""), budget.getArchiveId()+"/"+ transitionService.checksum(boardDecision.getOriginalFilename())));
                        }
                        Project project = projectService.get(budget.getProjectId());
                        transitionService.updatingPermissions("1","2", project, "APPROVE", Budget.class, budget.getId(), budget.getDate()+"");
                    } catch (Exception e) {
                        logger.error("Error occurred on approval of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage1)
                .target(BudgetStages.CANCELLED)
                .event(StageEvents.CANCEL)
                .action(context -> {
                    try {
                        transitionService.cancelBudget(context,"1");
                    } catch (Exception e) {
                        logger.error("Failed to cancel at Stage 1",e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage2)
                .target(BudgetStages.Stage2)
                .event(StageEvents.EDIT)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage1.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                    try {
                        if(req.getParameter("contractAmount") != null)
                            budget.setContractAmount(Double.parseDouble(req.getParameter("contractAmount")));

                        if(req.getParameter("regularAmount") != null)
                            budget.setRegularAmount(Double.parseDouble(req.getParameter("regularAmount")));

                        if(req.getParameter("tripAmount") !=null)
                            budget.setTripAmount(Double.parseDouble(req.getParameter("tripAmount")));

                        if(req.getParameter("servicesContractAmount") != null)
                            budget.setServicesContractAmount(Double.parseDouble(req.getParameter("servicesContractAmount")));

                        if(req.getParameter("comment") != null)
                            budget.setComment(req.getParameter("comment"));


                        if(req.getFile("technicalReport") !=null) {

                            MultipartFile technicalReport = req.getFile("technicalReport");
                            storeRESTClient.storeFile(technicalReport.getBytes(), budget.getArchiveId()+"/", transitionService.checksum(technicalReport.getOriginalFilename()));
                            budget.setTechnicalReport(new Attachment(technicalReport.getOriginalFilename(), FileUtils.extension(technicalReport.getOriginalFilename()),new Long(technicalReport.getSize()+""), budget.getArchiveId()+"/"+ transitionService.checksum(technicalReport.getOriginalFilename())));
                        }

                        if(req.getFile("boardDecision") !=null) {

                            MultipartFile boardDecision = req.getFile("boardDecision");
                            storeRESTClient.storeFile(boardDecision.getBytes(), budget.getArchiveId()+"/", transitionService.checksum(boardDecision.getOriginalFilename()));
                            budget.setBoardDecision(new Attachment(boardDecision.getOriginalFilename(), FileUtils.extension(boardDecision.getOriginalFilename()),new Long(boardDecision.getSize()+""), budget.getArchiveId()+"/"+ transitionService.checksum(boardDecision.getOriginalFilename())));
                        }
                        budgetService.update(budget,budget.getId());
                    } catch (Exception e) {
                        logger.error("Error occurred on edit budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage2)
                .target(BudgetStages.Stage1)
                .event(StageEvents.DOWNGRADE)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                    try {
                        Stage2 stage2 = Optional.ofNullable(budget.getStage2()).orElse(new Stage2(true, true, true));
                        stage2.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.downgradeBudget(context,"2","1",stage2);
                    } catch (Exception e) {
                        logger.error("Error occurred on downgrading budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage2)
                .target(BudgetStages.CANCELLED)
                .event(StageEvents.CANCEL)
                .action(context -> {
                    try {
                        transitionService.cancelBudget(context,"2");
                    } catch (Exception e) {
                        logger.error("Failed to cancel at Stage 2",e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage2)
                .target(BudgetStages.Stage4)
                .event(StageEvents.APPROVE)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage2.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage2 stage2 = new Stage2(true,true,true);
                        stage2.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.approveBudget(context,"2","4",stage2);
                    } catch (Exception e) {
                        logger.error("Error occurred on approval of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage2)
                .target(BudgetStages.REJECTED)
                .event(StageEvents.REJECT)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage2 stage2 = new Stage2(true,true,true);
                        stage2.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.rejectBudget(context, stage2,"2");
                    } catch (Exception e) {
                        logger.error("Error occurred on rejection of budget " + budget.getId());
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .and()
                .withExternal()
                .source(BudgetStages.Stage4)
                .target(BudgetStages.Stage4)
                .event(StageEvents.EDIT)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage2.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        transitionService.editBudget(context, budget.getStage2(), "2");
                    } catch (Exception e) {
                        logger.error("Error occurred on editing budget with id " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage4)
                .target(BudgetStages.Stage2)
                .event(StageEvents.DOWNGRADE)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage4 stage4 = Optional.ofNullable(budget.getStage4()).orElse(new Stage4());
                        stage4.setApproved(false);
                        stage4.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.downgradeBudget(context,"4","2",stage4);
                    } catch (Exception e) {
                        logger.error("Error occurred on downgrading budget with id " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }

                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage4)
                .target(BudgetStages.CANCELLED)
                .event(StageEvents.CANCEL)
                .action(context -> {
                    try {
                        transitionService.cancelBudget(context,"4");
                    } catch (Exception e) {
                        logger.error("Failed to cancel at Stage 4",e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage4)
                .target(BudgetStages.REJECTED)
                .event(StageEvents.REJECT)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage4 stage4 = Optional.ofNullable(budget.getStage4()).orElse(new Stage4());
                        stage4.setApproved(false);
                        transitionService.rejectBudget(context, stage4,"4");
                    } catch (Exception e) {
                        logger.error("Error occurred on rejection of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage4)
                .target(BudgetStages.Stage5a)
                .event(StageEvents.APPROVE)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage4.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage4 stage4 = new Stage4(true,true,true);
                        stage4.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.approveBudget(context,"4","5a",stage4);
                    } catch (Exception e) {
                        logger.error("Error occurred on approval of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage5a)
                .target(BudgetStages.Stage5a)
                .event(StageEvents.EDIT)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage4.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        transitionService.editBudget(context, budget.getStage4(), "4");
                    } catch (Exception e) {
                        logger.error("Error occurred on editing budget with id " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage5a)
                .target(BudgetStages.Stage4)
                .event(StageEvents.DOWNGRADE)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage5a stage5a = Optional.ofNullable(budget.getStage5a()).orElse(new Stage5a());
                        stage5a.setApproved(false);
                        stage5a.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.downgradeBudget(context,"5a","4",stage5a);
                    } catch (Exception e) {
                        logger.error("Error occurred on downgrading budget with id " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }

                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage5a)
                .target(BudgetStages.CANCELLED)
                .event(StageEvents.CANCEL)
                .action(context -> {
                    try {
                        transitionService.cancelBudget(context,"5a");
                    } catch (Exception e) {
                        logger.error("Failed to cancel at Stage 5a",e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage5a)
                .target(BudgetStages.REJECTED)
                .event(StageEvents.REJECT)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage5a stage5a = Optional.ofNullable(budget.getStage5a()).orElse(new Stage5a());
                        stage5a.setApproved(false);
                        transitionService.rejectBudget(context, stage5a,"5a");
                    } catch (Exception e) {
                        logger.error("Error occurred on rejection of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage5a)
                .target(BudgetStages.Stage6)
                .event(StageEvents.APPROVE)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage5a.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage5a stage5a = new Stage5a(true);
                        stage5a.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.approveBudget(context,"5a","6",stage5a);
                    } catch (Exception e) {
                        logger.error("Error occurred on approval of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage6)
                .target(BudgetStages.Stage6)
                .event(StageEvents.EDIT)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage5a.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        transitionService.editBudget(context, budget.getStage5a(), "5a");
                    } catch (Exception e) {
                        logger.error("Error occurred on editing budget with id " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage6)
                .target(BudgetStages.Stage5a)
                .event(StageEvents.DOWNGRADE)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage6 stage6 = Optional.ofNullable(budget.getStage6()).orElse(new Stage6());
                        stage6.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.downgradeBudget(context,"6","5a",stage6);
                    } catch (Exception e) {
                        logger.error("Error occurred on downgrading budget with id " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }

                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage6)
                .target(BudgetStages.CANCELLED)
                .event(StageEvents.CANCEL)
                .action(context -> {
                    try {
                        transitionService.cancelBudget(context,"6");
                    } catch (Exception e) {
                        logger.error("Failed to cancel at Stage 6",e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage6)
                .target(BudgetStages.REJECTED)
                .event(StageEvents.REJECT)
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage6 stage6 = Optional.ofNullable(budget.getStage6()).orElse(new Stage6());
                        stage6.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.rejectBudget(context, stage6,"6");
                    } catch (Exception e) {
                        logger.error("Error occurred on rejection of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.Stage6)
                .target(BudgetStages.FINISHED)
                .event(StageEvents.APPROVE)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage6.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    try {
                        Stage6 stage6 = new Stage6();
                        stage6.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.approveBudget(context,"6","6",stage6);
                    } catch (Exception e) {
                        logger.error("Error occurred on approval of budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(BudgetStages.FINISHED)
                .target(BudgetStages.FINISHED)
                .event(StageEvents.EDIT)
                .guard(stateContext -> transitionService.checkContainsBudget(stateContext, Stage5a.class))
                .action(context -> {
                    Budget budget = context.getMessage().getHeaders().get("budgetRequest", Budget.class);
                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                    try {
                        if(req.getParameter("contractAmount") != null)
                            budget.setContractAmount(Double.parseDouble(req.getParameter("contractAmount")));

                        if(req.getParameter("regularAmount") != null)
                            budget.setRegularAmount(Double.parseDouble(req.getParameter("regularAmount")));

                        if(req.getParameter("tripAmount") !=null)
                            budget.setTripAmount(Double.parseDouble(req.getParameter("tripAmount")));

                        if(req.getParameter("servicesContractAmount") != null)
                            budget.setServicesContractAmount(Double.parseDouble(req.getParameter("servicesContractAmount")));

                        if(req.getParameter("comment") != null)
                            budget.setComment(req.getParameter("comment"));

                        budgetService.update(budget,budget.getId());
                    } catch (Exception e) {
                        logger.error("Error occurred on edit budget " + budget.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
        ;
    }

}
