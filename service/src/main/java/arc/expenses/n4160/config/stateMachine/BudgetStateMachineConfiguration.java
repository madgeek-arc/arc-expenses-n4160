package arc.expenses.n4160.config.stateMachine;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.domain.BudgetStages;
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
@EnableStateMachineFactory
public class BudgetStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<BudgetStages, StageEvents> {

    private static Logger logger = LogManager.getLogger(BudgetStateMachineConfiguration.class);

    @Autowired
    private TransitionService transitionService;

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
    @DependsOn("factory")
    public void configure(StateMachineTransitionConfigurer<BudgetStages, StageEvents> transitions) throws Exception {

//        transitions.withExternal()
//                .source(BudgetStages.Stage1)
//                .target(BudgetStages.Stage2)
//                .event(StageEvents.APPROVE)
//                .guard(stateContext -> transitionService.checkContains(stateContext, Stage1.class))
//                .action(context -> {
//                    RequestApproval requestApproval = context.getMessage().getHeaders().get("requestApprovalObj", RequestApproval.class);
//                    MultipartHttpServletRequest req = (MultipartHttpServletRequest) context.getMessage().getHeaders().get("restRequest", HttpServletRequest.class);
//                    try {
//                        Stage1 stage1 = requestApproval.getStage1();
//                        stage1.setAmountInEuros(Double.parseDouble(req.getParameter("amountInEuros")));
//                        stage1.setFinalAmount(stage1.getAmountInEuros());
//                        stage1.setSubject(req.getParameter("subject"));
//                        stage1.setSupplier(Optional.ofNullable(req.getParameter("supplier")).orElse(stage1.getSupplier()));
//                        stage1.setSupplierSelectionMethod(Optional.ofNullable(Stage1.SupplierSelectionMethod.fromValue(req.getParameter("supplierSelectionMethod"))).orElse(stage1.getSupplierSelectionMethod()));
//                        transitionService.approveApproval(context,"1","2",stage1);
//                    } catch (Exception e) {
//                        logger.error("Error occurred on approval of request " + requestApproval.getId(),e);
//                        context.getStateMachine().setStateMachineError(new ServiceException(e.getMessage()));
//                        throw new ServiceException(e.getMessage());
//                    }
//                })
//                .and()
//                .withExternal()
//
//
//        ;
    }

}
