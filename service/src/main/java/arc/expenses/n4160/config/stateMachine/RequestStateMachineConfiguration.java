package arc.expenses.n4160.config.stateMachine;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.domain.NormalStages;
import arc.expenses.n4160.domain.StageEvents;
import arc.expenses.n4160.service.RequestApprovalServiceImpl;
import arc.expenses.n4160.service.RequestServiceImpl;
import arc.expenses.n4160.service.TransitionService;
import eu.openminted.registry.core.service.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Configuration
@EnableStateMachineFactory(name = "requestFactory")
public class RequestStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<NormalStages, StageEvents> {

    private static Logger logger = LogManager.getLogger(RequestStateMachineConfiguration.class);

    @Autowired
    private TransitionService transitionService;

    @Autowired
    private RequestApprovalServiceImpl requestApprovalService;

    @Autowired
    private RequestServiceImpl requestService;


    @Override
    public void configure(StateMachineStateConfigurer<NormalStages, StageEvents> states) throws Exception {
        states.withStates()
                .initial(NormalStages.Stage1)
                .choice(NormalStages.Stage5borFinish)
                .end(NormalStages.FINISHED)
                .end(NormalStages.REJECTED)
                .end(NormalStages.CANCELLED)
                .states(EnumSet.allOf(NormalStages.class))
            ;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<NormalStages, StageEvents> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(loggingListener());
    }

    public StateMachineListener<NormalStages, StageEvents> loggingListener() {
        return new StateMachineListenerAdapter<NormalStages, StageEvents>() {
            @Override
            public void stateChanged(State<NormalStages, StageEvents> from, State<NormalStages, StageEvents> to) {

            }

            @Override
            public void stateMachineError(StateMachine<NormalStages, StageEvents> stateMachine, Exception exception) {
                logger.info("Exception received from machine");
                stateMachine.getExtendedState().getVariables().put("error",exception.getMessage());
            }

            @Override
            public void eventNotAccepted(Message<StageEvents> event) {
                logger.error("Event not accepted: {}", event.getPayload());
            }
        };
    }

    @Override
    @DependsOn("requestFactory")
    public void configure(StateMachineTransitionConfigurer<NormalStages, StageEvents> transitions) throws Exception {

        transitions.withExternal()
                .source(NormalStages.Stage1)
                .target(NormalStages.Stage2)
                .event(StageEvents.APPROVE)
                .guard(stateContext -> transitionService.checkContains(stateContext, Stage1.class))
                .action(context -> {
                    RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                    try {
                        Stage1 stage1 = requestApproval.getStage1();
                        stage1.setAmountInEuros(Double.parseDouble(req.getParameter("amountInEuros")));
                        stage1.setFinalAmount(stage1.getAmountInEuros());
                        stage1.setSubject(req.getParameter("subject"));
                        stage1.setSupplier(Optional.ofNullable(req.getParameter("supplier")).orElse(stage1.getSupplier()));
                        stage1.setSupplierSelectionMethod(Optional.ofNullable(Stage1.SupplierSelectionMethod.fromValue(req.getParameter("supplierSelectionMethod"))).orElse(stage1.getSupplierSelectionMethod()));
                        transitionService.approveApproval(context,"1","2",stage1);
                    } catch (Exception e) {
                        logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(NormalStages.Stage1)
                .target(NormalStages.CANCELLED)
                .event(StageEvents.CANCEL)
                .action(context -> {
                    try {
                        transitionService.cancelRequestApproval(context,"1");
                    } catch (Exception e) {
                        logger.error("Failed to cancel at Stage 1",e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
            .withExternal()
                .source(NormalStages.Stage2)
                .target(NormalStages.Stage2)
                .event(StageEvents.EDIT)
                .guard(stateContext -> transitionService.checkContains(stateContext, Stage1.class))
                .action(context -> {
                    RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                    try {
                        transitionService.editApproval(context, requestApproval.getStage1(), "1");
                    } catch (Exception e) {
                        logger.error("Error occurred on downgrading approval of request " + requestApproval.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                .source(NormalStages.Stage2)
                .target(NormalStages.Stage1)
                .event(StageEvents.DOWNGRADE)
                .action(context -> {
                    RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                    try {
                        Stage2 stage2 = new Stage2(Optional.ofNullable(Boolean.parseBoolean(req.getParameter("checkFeasibility"))).orElse(false),Optional.ofNullable(Boolean.parseBoolean(req.getParameter("checkNecessity"))).orElse(false),true);
                        stage2.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.downgradeApproval(context,"2","1",stage2);
                    } catch (Exception e) {
                        logger.error("Error occurred on downgrading approval of request " + requestApproval.getId(),e);
                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                        throw new ServiceException(e.getMessage());
                    }
                })
                .and()
                .withExternal()
                    .source(NormalStages.Stage2)
                    .target(NormalStages.Stage3)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage2.class))
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            Stage2 stage2 = new Stage2(true,true,true);
                            stage2.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approveApproval(context,"2","3",stage2);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage2)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestApproval(context,"2");
                        } catch (Exception e) {
                            logger.error("Failed to cancel at Stage 2",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage2)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            Stage2 stage2 = new Stage2(true,true,true);
                            stage2.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.rejectApproval(context, stage2,"2");
                        } catch (Exception e) {
                            logger.error("Error occurred on rejection of request approval " + requestApproval.getId());
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage3)
                    .target(NormalStages.Stage2)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        Stage3 stage3 = Optional.ofNullable(requestApproval.getStage3()).orElse(new Stage3());
                        stage3.setDate(new Date().toInstant().toEpochMilli());
                        transitionService.downgradeApproval(context,"3","2",stage3);
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage3)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestApproval(context,"3");
                        } catch (Exception e) {
                            logger.error("Failed to cancel at Stage 3",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage3)
                    .target(NormalStages.Stage3)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage2.class))
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            transitionService.editApproval(context, requestApproval.getStage2(), "2");
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage3)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            Stage3 stage3 = Optional.ofNullable(requestApproval.getStage3()).orElse(new Stage3());
                            stage3.setApproved(false);
                            transitionService.rejectApproval(context, stage3,"3");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage5b)
                    .target(NormalStages.Stage3)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            Stage5b stage5b = Optional.ofNullable(requestApproval.getStage5b()).orElse(new Stage5b());
                            stage5b.setApproved(false);
                            transitionService.downgradeApproval(context,"5b","3",stage5b);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }

                    })
                    .and()
                    .withChoice()
                    .source(NormalStages.Stage5borFinish)
                    .first(NormalStages.FINISHED, context -> {
                        if(!transitionService.checkContains(context,Stage3.class))
                            return false;

                        try {
                            RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                            Request request = requestService.get(requestApproval.getRequestId());
                            if(
                                    requestApproval.getStage1().getAmountInEuros()>20000 ||
                                            requestApproval.getStage1().getSupplierSelectionMethod() == Stage1.SupplierSelectionMethod.AWARD_PROCEDURE ||
                                            request.getType() == Request.Type.CONTRACT ||
                                            request.getType() == Request.Type.SERVICES_CONTRACT
                            )
                                return true;

                            return false;
                        } catch (Exception e) {
                            logger.error("Error occurred on choice of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }

                    }, context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

                            Stage3 stage3 = new Stage3(true,true,true);
                            stage3.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approveApproval(context,"3","5b",stage3);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .last(NormalStages.Stage5b, context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);

                            Stage3 stage3 = new Stage3(true,true,true);
                            stage3.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approveApproval(context,"3","5b",stage3);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage3)
                    .target(NormalStages.Stage5borFinish)
                    .event(StageEvents.APPROVE)
                    .and()
                .withExternal()
                    .source(NormalStages.Stage5b)
                    .target(NormalStages.FINISHED)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage5b.class))
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                        try {
                            Stage1 stage1 = requestApproval.getStage1();
                            stage1.setAmountInEuros(Double.parseDouble(req.getParameter("amountInEuros")));
                            stage1.setFinalAmount(stage1.getAmountInEuros());
                            stage1.setSupplier(Optional.ofNullable(req.getParameter("supplier")).orElse(stage1.getSupplier()));
                            requestApproval.setStage1(stage1);
                            requestApprovalService.update(requestApproval, requestApproval.getId());

                            Stage5b stage5b = new Stage5b(true);
                            stage5b.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approveApproval(context,"5b","6",stage5b);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage5b)
                    .target(NormalStages.Stage5b)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage5a.class))
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                        try {
                            transitionService.editApproval(context, requestApproval.getStage3(), "3");
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage5b)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestApproval(context,"5b");
                        } catch (Exception e) {
                            logger.error("Failed to cancel at Stage 5b",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage5b)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
                        try {
                            Stage5b stage5b = Optional.ofNullable(requestApproval.getStage5b()).orElse(new Stage5b());
                            stage5b.setApproved(false);
                            transitionService.rejectApproval(context, stage5b,"5b");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage7)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage7 stage7 = Optional.ofNullable(payment.getStage7()).orElse(new Stage7());
                            stage7.setApproved(false);
                            stage7.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.rejectPayment(context, stage7,"7");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage7)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"7");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }

                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage7)
                    .target(NormalStages.Stage7a)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage7.class))
                    .event(StageEvents.APPROVE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                            RequestApproval requestApproval = requestApprovalService.getApproval(requestPayment.getRequestId());
                            Stage1 stage1 = requestApproval.getStage1();
                            String finalAmount = Optional.ofNullable(req.getParameter("finalAmount")).orElse("");
                            if(!finalAmount.isEmpty())
                                stage1.setFinalAmount(Double.parseDouble(finalAmount));

                            requestApproval.setStage1(stage1);
                            requestApprovalService.update(requestApproval, requestApproval.getId());

                            Stage7 stage7 = Optional.ofNullable(requestPayment.getStage7()).orElse(new Stage7());
                            stage7.setApproved(true);
                            stage7.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"7","7a",stage7);
                        } catch (Exception e) {
                            logger.error("Error occurred on upgrading payment of request " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage7a)
                    .target(NormalStages.Stage8)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage7a.class))
                    .action(context -> {
                        try {
                            HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                            String loan = Optional.ofNullable(req.getParameter("loan")).orElse("false");
                            Stage7a stage7a = new Stage7a(true,Boolean.parseBoolean(loan));
                            stage7a.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"7a","8",stage7a);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage7a)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage7a stage7a = Optional.ofNullable(payment.getStage7a()).orElse(new Stage7a());
                            stage7a.setApproved(false);
                            transitionService.rejectPayment(context, stage7a,"7a");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage7a)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            transitionService.cancelRequestPayment(context, "7a");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage7a)
                    .target(NormalStages.Stage7a)
                    .event(StageEvents.EDIT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                            RequestApproval requestApproval = requestApprovalService.getApproval(payment.getRequestId());
                            Stage1 stage1 = requestApproval.getStage1();
                            String finalAmount = Optional.ofNullable(req.getParameter("finalAmount")).orElse("");
                            if(!finalAmount.isEmpty())
                                stage1.setFinalAmount(Double.parseDouble(finalAmount));
                            requestApproval.setStage1(stage1);
                            requestApprovalService.update(requestApproval, requestApproval.getId());


                            transitionService.editPayment(context, payment.getStage7(),"7");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage7a)
                    .target(NormalStages.Stage7)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage7a stage7a = Optional.ofNullable(requestPayment.getStage7a()).orElse(new Stage7a());
                            stage7a.setApproved(false);
                            stage7a.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.downgradePayment(context,"7a","7",stage7a);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage8)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage8 stage8 = Optional.ofNullable(payment.getStage8()).orElse(new Stage8());
                            stage8.setApproved(false);
                            transitionService.rejectPayment(context, stage8,"8");
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage8)
                    .target(NormalStages.Stage8)
                    .event(StageEvents.EDIT)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);

                        try {
                            RequestApproval requestApproval = requestApprovalService.getApproval(requestPayment.getRequestId());
                            HttpServletRequest req = context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
                            Stage1 stage1 = requestApproval.getStage1();
                            stage1.setFinalAmount(Double.parseDouble(req.getParameter("finalAmount")));
                            requestApproval.setStage1(stage1);
                            requestApprovalService.update(requestApproval, requestApproval.getId());
                            transitionService.editPayment(context, requestPayment.getStage7a(), "7a");

                        } catch (Exception e) {
                            logger.error("Error occurred on editing of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage8)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"8");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage8)
                    .target(NormalStages.Stage7a)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage8 stage8 = Optional.ofNullable(requestPayment.getStage8()).orElse(new Stage8());
                            stage8.setApproved(false);
                            transitionService.downgradePayment(context,"8","7a",stage8);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgradePayment of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage8)
                    .target(NormalStages.Stage9)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage8.class))
                    .action(context -> {
                        try {
                            Stage8 stage8 = new Stage8(true, true, true);
                            stage8.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"8","9",stage8);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage9)
                    .target(NormalStages.Stage9)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage8.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            transitionService.editPayment(context, requestPayment.getStage8(), "8");
                        } catch (Exception e) {
                            logger.error("Error occurred on editing of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage9)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage9 stage9 = Optional.ofNullable(payment.getStage9()).orElse(new Stage9());
                            stage9.setApproved(false);
                            transitionService.rejectPayment(context, stage9,"9");
                        } catch (Exception e) {
                            logger.error("Error occurred on rejection of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage9)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"9");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                .and()
                .withExternal()
                    .source(NormalStages.Stage9)
                    .target(NormalStages.Stage8)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage9 stage9 = Optional.ofNullable(requestPayment.getStage9()).orElse(new Stage9());
                            stage9.setApproved(false);
                            transitionService.downgradePayment(context,"9","8",stage9);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage9)
                    .target(NormalStages.Stage10)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage9.class))
                    .action(context -> {
                        try {
                            Stage9 stage9 = new Stage9(true, true, true);
                            stage9.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"9","10",stage9);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage10)
                    .target(NormalStages.Stage10)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage9.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            transitionService.editPayment(context, requestPayment.getStage9(), "9");
                        } catch (Exception e) {
                            logger.error("Error occurred on editing of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                  .and()
                .withExternal()
                    .source(NormalStages.Stage10)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"10");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                .and()
                .withExternal()
                    .source(NormalStages.Stage10)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage10 stage10 = Optional.ofNullable(payment.getStage10()).orElse(new Stage10());
                            stage10.setApproved(false);
                            transitionService.rejectPayment(context, stage10,"10");
                        } catch (Exception e) {
                            logger.error("Error occurred on rejection of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage10)
                    .target(NormalStages.Stage9)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage10 stage10 = Optional.ofNullable(requestPayment.getStage10()).orElse(new Stage10());
                            stage10.setApproved(false);
                            transitionService.downgradePayment(context,"10","9",stage10);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage10)
                    .target(NormalStages.Stage11)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage10.class))
                    .action(context -> {
                        try {
                            Stage10 stage10 =new Stage10(true);
                            stage10.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"10","11", stage10);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage11)
                    .target(NormalStages.Stage11)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage10.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            transitionService.editPayment(context, requestPayment.getStage10(), "10");
                        } catch (Exception e) {
                            logger.error("Error occurred on editing of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                 .and()
                .withExternal()
                    .source(NormalStages.Stage11)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"11");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                .and()
                .withExternal()
                    .source(NormalStages.Stage11)
                    .target(NormalStages.Stage10)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage11 stage11 = Optional.ofNullable(requestPayment.getStage11()).orElse(new Stage11());
                            transitionService.downgradePayment(context,"11","10",stage11);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage11)
                    .target(NormalStages.Stage12)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage11.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage11 stage11 = Optional.ofNullable(requestPayment.getStage11()).orElse(new Stage11());
                            stage11.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"11","12",stage11);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage12)
                    .target(NormalStages.Stage11)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage12 stage12 = Optional.ofNullable(requestPayment.getStage12()).orElse(new Stage12());
                            stage12.setApproved(false);
                            transitionService.downgradePayment(context,"12","11",stage12);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage12)
                    .target(NormalStages.Stage12)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage11.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            transitionService.editPayment(context, requestPayment.getStage11(), "11");
                        } catch (Exception e) {
                            logger.error("Error occurred on editing of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                 .and()
                .withExternal()
                    .source(NormalStages.Stage12)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"12");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                .and()
                .withExternal()
                    .source(NormalStages.Stage12)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage12 stage12 = Optional.ofNullable(payment.getStage12()).orElse(new Stage12());
                            transitionService.rejectPayment(context, stage12,"12");
                        } catch (Exception e) {
                            logger.error("Error occurred on rejection of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage12)
                    .target(NormalStages.Stage13)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage12.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage12 stage12 = Optional.ofNullable(requestPayment.getStage12()).orElse(new Stage12(true));
                            stage12.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"12","13",stage12);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage13)
                    .target(NormalStages.CANCELLED)
                    .event(StageEvents.CANCEL)
                    .action(context -> {
                        try {
                            transitionService.cancelRequestPayment(context,"13");
                        } catch (Exception e) {
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                .and()
                .withExternal()
                    .source(NormalStages.Stage13)
                    .target(NormalStages.Stage12)
                    .event(StageEvents.DOWNGRADE)
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage13 stage13 = Optional.ofNullable(requestPayment.getStage13()).orElse(new Stage13());
                            stage13.setApproved(false);
                            transitionService.downgradePayment(context,"13","12",stage13);
                        } catch (Exception e) {
                            logger.error("Error occurred on downgrading payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                    .withExternal()
                    .source(NormalStages.Stage13)
                    .target(NormalStages.Stage13)
                    .event(StageEvents.EDIT)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage12.class))
                    .action(context -> {
                        RequestPayment requestPayment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            transitionService.editPayment(context, requestPayment.getStage12(), "12");
                        } catch (Exception e) {
                            logger.error("Error occurred on editing of payment " + requestPayment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage13)
                    .target(NormalStages.REJECTED)
                    .event(StageEvents.REJECT)
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage13 stage13 = Optional.ofNullable(payment.getStage13()).orElse(new Stage13());
                            transitionService.rejectPayment(context, stage13,"13");
                        } catch (Exception e) {
                            logger.error("Error occurred on rejection of payment " + payment.getId(),e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })
                    .and()
                .withExternal()
                    .source(NormalStages.Stage13)
                    .target(NormalStages.FINISHED)
                    .event(StageEvents.APPROVE)
                    .guard(stateContext -> transitionService.checkContains(stateContext, Stage13.class))
                    .action(context -> {
                        RequestPayment payment = context.getMessage().getHeaders().get("paymentObj", RequestPayment.class);
                        try {
                            Stage13 stage13 = Optional.ofNullable(payment.getStage13()).orElse(new Stage13(true));
                            stage13.setDate(new Date().toInstant().toEpochMilli());
                            transitionService.approvePayment(context,"13","13",stage13);
                        } catch (Exception e) {
                            logger.error("Error occurred on approval of request ",e);
                            context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
                            throw new ServiceException(e.getMessage());
                        }
                    })

        ;
    }

}
