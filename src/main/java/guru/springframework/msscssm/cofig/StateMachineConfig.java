package guru.springframework.msscssm.cofig;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;


    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {

        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                .action(preAuthAction).guard(paymentIdGuard)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .action(preAuthApprovedAction)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .action(preAuthDeclinedAction)
                //preauth to uth
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                .action(authAction)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
                .action(authApprovedAction)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(
                        PaymentEvent.AUTH_DECLINED
                ).action(authDeclinedAction)

        ;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter =  new StateMachineListenerAdapter<>(){

            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                //super.stateChanged(from, to);
                log.info(String.format("state changed(from : %s, to: %s)", from, to));
            }
        };

        config.withConfiguration().listener(adapter);
    }


/*
    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
        return stateContext -> {
            return stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
        };
    }


    public Action<PaymentState, PaymentEvent> preAuthAction() {

        return stateContext -> {
            System.out.println("Pre Auth was called");
            if (new Random().nextInt(10) < 8) {
                System.out.println("Pre auth Approved");
                stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            } else {
                System.out.println("Pre Auth Declined No Credit Limit");
                stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build()
                );
            }
        };
    }

    public Action<PaymentState, PaymentEvent> authAction(){

        return  stateContext -> {
            System.out.println("Auth was called");

            if(new Random().nextInt(10) < 8){
                System.out.println("Auth Approved");
                stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }else{
                System.out.println("Auth Declined Credit!!!!!!");
                stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }

        };

    }
*/


}
