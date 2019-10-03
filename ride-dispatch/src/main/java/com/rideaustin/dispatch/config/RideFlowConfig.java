package com.rideaustin.dispatch.config;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Named;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;

import com.rideaustin.application.cache.impl.JedisClient;
import com.rideaustin.dispatch.InceptionMachinesSubscriber;
import com.rideaustin.dispatch.InceptionMachinesTracker;
import com.rideaustin.dispatch.actions.AcceptRideAction;
import com.rideaustin.dispatch.actions.AdminCancelAction;
import com.rideaustin.dispatch.actions.DispatchDeclineAction;
import com.rideaustin.dispatch.actions.DispatchNotAcceptedAction;
import com.rideaustin.dispatch.actions.DriverCancelAction;
import com.rideaustin.dispatch.actions.DriverReachAction;
import com.rideaustin.dispatch.actions.DumpContextAction;
import com.rideaustin.dispatch.actions.EndRideAction;
import com.rideaustin.dispatch.actions.ForceRedispatchAction;
import com.rideaustin.dispatch.actions.HandshakeFailedAction;
import com.rideaustin.dispatch.actions.NoAvailableDriverAction;
import com.rideaustin.dispatch.actions.PreauthorizationFailedAction;
import com.rideaustin.dispatch.actions.RedispatchOnCancelAction;
import com.rideaustin.dispatch.actions.ReleaseRequestedDriversAction;
import com.rideaustin.dispatch.actions.RiderCancelAction;
import com.rideaustin.dispatch.actions.SearchDriversAction;
import com.rideaustin.dispatch.actions.SendDispatchRequestAction;
import com.rideaustin.dispatch.actions.SendHandshakeRequestAction;
import com.rideaustin.dispatch.actions.StartRideAction;
import com.rideaustin.dispatch.actions.UpdateCommentAction;
import com.rideaustin.dispatch.actions.UpdateDestinationAction;
import com.rideaustin.dispatch.aop.ActionAspect;
import com.rideaustin.dispatch.error.CompositeErrorHandlingAction;
import com.rideaustin.dispatch.error.ErrorHandlingAction;
import com.rideaustin.dispatch.error.StopMachineAction;
import com.rideaustin.dispatch.guards.AcceptanceGuard;
import com.rideaustin.dispatch.guards.AuthorizedAdminGuard;
import com.rideaustin.dispatch.guards.AuthorizedDriverGuard;
import com.rideaustin.dispatch.guards.AuthorizedRiderGuard;
import com.rideaustin.dispatch.guards.DeclineGuard;
import com.rideaustin.dispatch.guards.DispatchNotAcceptedGuard;
import com.rideaustin.dispatch.guards.EndRideGuard;
import com.rideaustin.dispatch.guards.ForceRedispatchGuard;
import com.rideaustin.dispatch.guards.RedispatchOnCancelGuard;
import com.rideaustin.dispatch.guards.UpdateCommentGuard;
import com.rideaustin.dispatch.guards.UpdateDestinationGuard;
import com.rideaustin.dispatch.persist.RARedisStateMachineContextRepository;
import com.rideaustin.dispatch.persist.RARedisStateMachinePersister;
import com.rideaustin.dispatch.service.DefaultConsecutiveDeclineUpdateService;
import com.rideaustin.dispatch.service.DispatchDeclineRequestChecker;
import com.rideaustin.dispatch.service.RideFlowStateMachineListener;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.dispatch.service.queue.QueueConsecutiveDeclineUpdateService;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.areaqueue.AreaQueuePenaltyService;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

@Configuration
@EnableStateMachine
@DependsOn("flyway")
@EnableAspectJAutoProxy
public class RideFlowConfig {

  @Bean("taskExecutor")
  public TaskScheduler taskScheduler(Environment environment) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(environment.getProperty("rideflow.taskexecutor.pool", Integer.class, 10));
    scheduler.setDaemon(true);
    return scheduler;
  }

  @Bean
  public RedisTemplate rideFlowRedisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  @Bean
  public RideFlowStateMachineProvider stateMachineProvider(Environment environment, BeanFactory beanFactory,
    RideDslRepository rideDslRepository, StateMachinePersist<States, Events, String> stateMachinePersist,
    @Named("rideFlowRedisTemplate") RedisTemplate redisTemplate, @Named("inceptionMachinesTopic") ChannelTopic inceptionMachinesTopic,
    RideDispatchServiceConfig config, AreaService areaService) {
    return new RideFlowStateMachineProvider(environment, beanFactory, config, areaService, rideDslRepository,
      stateMachinePersist, redisTemplate, inceptionMachinesTopic);
  }

  @Bean
  @Scope(value = "request")
  public ScopedProxyFactoryBean stateMachine() {
    ScopedProxyFactoryBean pfb = new ScopedProxyFactoryBean();
    pfb.setTargetBeanName("stateMachineTarget");
    return pfb;
  }

  @Bean(name = {"stateMachineTarget", "inceptionStateMachine"})
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public StateMachine<States, Events> stateMachine(BeanFactory beanFactory, RideDispatchServiceConfig config, Environment environment) throws Exception {
    StateMachineBuilder.Builder<States, Events> builder = StateMachineBuilder.builder();

    builder.configureConfiguration()
      .withConfiguration()
      .taskScheduler(taskScheduler(environment))
      .beanFactory(beanFactory)
      .listener(rideFlowStateMachineListener())
      .autoStartup(false);

    /**
     * configure states in ride flow state machine
     */
    builder.configureStates()
      .withStates()
      .initial(States.REQUESTED)  //initial state - REQUESTED
      .stateDo(                   //start SearchDriverAction when machine starts
        States.REQUESTED, searchDriversAction(), compositeErrorHandlingAction(noAvailableDriverAction(),
        new StopMachineAction()))
      .stateDo(States.HANDSHAKE_PENDING, sendHandshakeRequestAction(), errorHandlingAction())
      .stateDo(States.DISPATCH_PENDING, sendDispatchRequestAction(), errorHandlingAction())
      .choice(States.DRIVER_CANCELLED) //handle redispatch on cancel - we can go either back to REQUESTED or forward to DRIVER_CANCELLED
      .state(States.DRIVER_ASSIGNED, Events.START_RIDE, Events.END_RIDE) //list deferred events to support bulk events processing
      .stateDo(States.DRIVER_ASSIGNED, acceptRideAction(), errorHandlingAction())
      .state(States.DRIVER_REACHED, Events.END_RIDE) //list deferred events to support bulk events processing
      .stateDo(States.DRIVER_REACHED, driverReachAction(), errorHandlingAction())
      .stateDo(States.RIDER_CANCELLED, riderCancelAction(), errorHandlingAction())
      .stateDo(States.ADMIN_CANCELLED, adminCancelAction(), errorHandlingAction())
      .states(EnumSet.allOf(States.class)) //list all other states
      .end(States.ENDED) //terminal state - ENDED
      ;

    builder.configureTransitions()
      // REQUESTED transitions
      .withExternal()
        .source(States.REQUESTED)
        .target(States.DISPATCH_PENDING)
        .event(Events.DISPATCH_REQUEST_SEND)
      .and()
      .withExternal()
        .source(States.REQUESTED)
        .target(States.HANDSHAKE_PENDING)
        .event(Events.HANDSHAKE_REQUEST_SEND)
      .and()
      .withExternal()
        .source(States.REQUESTED)
        .target(States.RIDER_CANCELLED)
        .event(Events.RIDER_CANCEL)
        .guard(authorizedRiderGuard())
      .and()
      .withExternal()
        .source(States.REQUESTED)
        .target(States.ADMIN_CANCELLED)
        .event(Events.ADMIN_CANCEL)
        .guard(authorizedAdminGuard())
      .and()
      .withExternal()
        .source(States.REQUESTED)
        .target(States.NO_AVAILABLE_DRIVER)
        .event(Events.NO_DRIVERS_AVAILABLE)
        .action(noAvailableDriverAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.REQUESTED)
        .target(States.NO_AVAILABLE_DRIVER)
        .event(Events.ABORT_PREAUTHORIZATION_FAILED)
        .action(noAvailableDriverAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.REQUESTED)
        .event(Events.UPDATE_DESTINATION)
        .guard(updateDestinationGuard())
        .action(updateDestinationAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.REQUESTED)
        .event(Events.UPDATE_COMMENT)
        .guard(updateCommentGuard())
        .action(updateCommentAction(), errorHandlingAction())
      .and()
      // HANDSHAKE_PENDING transitions
      .withExternal()
        .source(States.HANDSHAKE_PENDING)
        .target(States.REQUESTED)
        .timerOnce(config.getDispatchAllowanceTimeoutWithCoverage() * 1000L)
        .action(handshakeFailedAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.HANDSHAKE_PENDING)
        .target(States.RIDER_CANCELLED)
        .event(Events.RIDER_CANCEL)
        .guard(authorizedRiderGuard())
      .and()
      .withExternal()
        .source(States.HANDSHAKE_PENDING)
        .target(States.ADMIN_CANCELLED)
        .event(Events.ADMIN_CANCEL)
        .guard(authorizedAdminGuard())
      .and()
      .withExternal()
        .source(States.HANDSHAKE_PENDING)
        .event(Events.HANDSHAKE_ACKNOWLEDGE)
        .target(States.DISPATCH_PENDING)
      .and()
      .withExternal()
        .source(States.HANDSHAKE_PENDING)
        .target(States.NO_AVAILABLE_DRIVER)
        .event(Events.ABORT_PREAUTHORIZATION_FAILED)
        .action(noAvailableDriverAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.HANDSHAKE_PENDING)
        .event(Events.UPDATE_DESTINATION)
        .guard(updateDestinationGuard())
        .action(updateDestinationAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.HANDSHAKE_PENDING)
        .event(Events.UPDATE_COMMENT)
        .guard(updateCommentGuard())
        .action(updateCommentAction(), errorHandlingAction())
      .and()
      // DISPATCH_PENDING transitions
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.DRIVER_ASSIGNED)
        .event(Events.DISPATCH_REQUEST_ACCEPT)
        .guard(acceptanceGuard())
      .and()
      /**
       * handle cases when driver has accepted the ride but state was not updated
       */
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.DRIVER_REACHED)
        .event(Events.DRIVER_REACH)
        .guard(acceptanceGuard())
        .action(acceptRideAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.REQUESTED)
        .event(Events.DISPATCH_REQUEST_DECLINE)
        .guard(declineGuard())
        .action(dispatchDeclineAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.REQUESTED)
        .timerOnce(config.getPerDriverWaitTime() * 1000L)
        .guard(dispatchNotAcceptedGuard())
        .action(dispatchNotAcceptedAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.RIDER_CANCELLED)
        .event(Events.RIDER_CANCEL)
        .guard(authorizedRiderGuard())
      .and()
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.ADMIN_CANCELLED)
        .event(Events.ADMIN_CANCEL)
        .guard(authorizedAdminGuard())
      .and()
      .withExternal()
        .source(States.DISPATCH_PENDING)
        .target(States.NO_AVAILABLE_DRIVER)
        .event(Events.ABORT_PREAUTHORIZATION_FAILED)
        .action(preauthorizationFailedAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.DISPATCH_PENDING)
        .event(Events.UPDATE_DESTINATION)
        .guard(updateDestinationGuard())
        .action(updateDestinationAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.DISPATCH_PENDING)
        .event(Events.UPDATE_COMMENT)
        .guard(updateCommentGuard())
        .action(updateCommentAction(), errorHandlingAction())
      .and()
      // DRIVER_ASSIGNED transitions
      .withExternal()
        .source(States.DRIVER_ASSIGNED)
        .target(States.DRIVER_REACHED)
        .event(Events.DRIVER_REACH)
      .and()
      .withExternal()
        .source(States.DRIVER_ASSIGNED)
        .target(States.RIDER_CANCELLED)
        .event(Events.RIDER_CANCEL)
        .guard(authorizedRiderGuard())
      .and()
      .withExternal()
        .source(States.DRIVER_ASSIGNED)
        .target(States.ADMIN_CANCELLED)
        .event(Events.ADMIN_CANCEL)
        .guard(authorizedAdminGuard())
      .and()
      .withExternal()
        .source(States.DRIVER_ASSIGNED)
        .target(States.DRIVER_CANCELLED)
        .event(Events.DRIVER_CANCEL)
        .guard(authorizedDriverGuard())
      .and()
      .withExternal()
        .source(States.DRIVER_ASSIGNED)
        .target(States.REQUESTED)
        .event(Events.FORCE_REDISPATCH)
        .action(forceRedispatchAction(), errorHandlingAction())
        .guard(forceRedispatchGuard())
      .and()
      .withInternal()
        .source(States.DRIVER_ASSIGNED)
        .event(Events.UPDATE_DESTINATION)
        .guard(updateDestinationGuard())
        .action(updateDestinationAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.DRIVER_ASSIGNED)
        .event(Events.UPDATE_COMMENT)
        .guard(updateCommentGuard())
        .action(updateCommentAction(), errorHandlingAction())
      .and()
      // DRIVER_REACHED transitions
      .withExternal()
        .source(States.DRIVER_REACHED)
        .target(States.ACTIVE)
        .event(Events.START_RIDE)
        .guard(authorizedDriverGuard())
        .action(startRideAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.DRIVER_REACHED)
        .target(States.RIDER_CANCELLED)
        .event(Events.RIDER_CANCEL)
        .guard(authorizedRiderGuard())
      .and()
      .withExternal()
        .source(States.DRIVER_REACHED)
        .target(States.DRIVER_CANCELLED)
        .event(Events.DRIVER_CANCEL)
        .guard(authorizedDriverGuard())
      .and()
      .withExternal()
        .source(States.DRIVER_REACHED)
        .target(States.ADMIN_CANCELLED)
        .event(Events.ADMIN_CANCEL)
        .guard(authorizedAdminGuard())
      .and()
      .withInternal()
        .source(States.DRIVER_REACHED)
        .event(Events.UPDATE_DESTINATION)
        .guard(updateDestinationGuard())
        .action(updateDestinationAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.DRIVER_REACHED)
        .event(Events.UPDATE_COMMENT)
        .guard(updateCommentGuard())
        .action(updateCommentAction(), errorHandlingAction())
      .and()
      // ACTIVE transitions
      .withExternal()
        .source(States.ACTIVE)
        .target(States.COMPLETED)
        .event(Events.END_RIDE)
        .guard(endRideGuard())
        .action(endRideAction(), errorHandlingAction())
      .and()
      .withExternal()
        .source(States.ACTIVE)
        .target(States.ADMIN_CANCELLED)
        .event(Events.ADMIN_CANCEL)
        .guard(authorizedAdminGuard())
      .and()
      .withInternal()
        .source(States.ACTIVE)
        .event(Events.UPDATE_DESTINATION)
        .guard(updateDestinationGuard())
        .action(updateDestinationAction(), errorHandlingAction())
      .and()
      .withInternal()
        .source(States.ACTIVE)
        .event(Events.UPDATE_COMMENT)
        .guard(updateCommentGuard())
        .action(updateCommentAction(), errorHandlingAction())
      .and()
      // terminal transitions
      .withLocal()
        .source(States.NO_AVAILABLE_DRIVER)
        .timerOnce(10000L)
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      .withLocal()
        .source(States.COMPLETED)
        .timerOnce(1000L)
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      .withLocal()
        .source(States.RIDER_CANCELLED)
        .timerOnce(1000L)
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      .withChoice()
        .source(States.DRIVER_CANCELLED)
        .first(States.REQUESTED, redispatchOnCancelGuard(), redispatchOnCancelAction(), errorHandlingAction())
        .last(States.ENDED, driverCancelAction(), errorHandlingAction())
      .and()
      .withLocal()
        .source(States.ADMIN_CANCELLED)
        .timerOnce(adminCancelledExpiration())
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      // abnormal expiration transitions
      .withLocal()
        .source(States.REQUESTED)
        .timerOnce(inceptionMachineExpiration(config))
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      .withLocal()
        .source(States.DISPATCH_PENDING)
        .timerOnce(inceptionMachineExpiration(config))
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      .withLocal()
        .source(States.DRIVER_ASSIGNED)
        .timerOnce(inceptionMachineExpiration(config))
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction())
      .and()
      .withLocal()
        .source(States.DRIVER_REACHED)
        .timerOnce(inceptionMachineExpiration(config))
        .target(States.ENDED)
        .action(dumpContextAction(), errorHandlingAction());
    return builder.build();
  }

  @Bean
  public Long adminCancelledExpiration() {
    return 1L;
  }

  @Bean
  public Long riderCancelledExpiration() {
    return 1L;
  }

  @Bean
  public Long completedExpiration() {
    return 1L;
  }

  @Bean
  @Profile("!itest")
  public Long noAvailableDriverExpiration() {
    return 30_000L;
  }

  @Bean
  public Long inceptionMachineExpiration(RideDispatchServiceConfig config) {
    return config.getInceptionMachineExpiration();
  }

  @Bean
  public Guard<States, Events> forceRedispatchGuard() {
    return new ForceRedispatchGuard();
  }

  @Bean
  public Guard<States, Events> endRideGuard() {
    return new EndRideGuard();
  }

  @Bean
  public Guard<States, Events> updateCommentGuard() {
    return new UpdateCommentGuard();
  }

  @Bean
  public Guard<States, Events> updateDestinationGuard() {
    return new UpdateDestinationGuard();
  }

  @Bean
  public Guard<States, Events> dispatchNotAcceptedGuard() {
    return new DispatchNotAcceptedGuard();
  }

  @Bean
  public Guard<States, Events> declineGuard() {
    return new DeclineGuard();
  }

  @Bean
  public Guard<States, Events> acceptanceGuard() {
    return new AcceptanceGuard();
  }

  @Bean
  public Guard<States, Events> authorizedRiderGuard() {
    return new AuthorizedRiderGuard();
  }

  @Bean
  public Guard<States, Events> authorizedDriverGuard() {
    return new AuthorizedDriverGuard();
  }

  @Bean
  public Guard<States, Events> authorizedAdminGuard() {
    return new AuthorizedAdminGuard();
  }

  @Bean
  public Guard<States, Events> redispatchOnCancelGuard() {
    return new RedispatchOnCancelGuard();
  }

  @Bean
  public Action<States, Events> handshakeFailedAction() {
    return new HandshakeFailedAction();
  }

  @Bean
  public Action<States, Events> sendHandshakeRequestAction() {
    return new SendHandshakeRequestAction();
  }

  @Bean
  public Action<States, Events> dumpContextAction() {
    return new DumpContextAction();
  }

  @Bean
  public Action<States, Events> preauthorizationFailedAction() {
    return new PreauthorizationFailedAction((ReleaseRequestedDriversAction) releaseRequestedDriversAction());
  }

  @Bean
  public Action<States, Events> noAvailableDriverAction() {
    return new NoAvailableDriverAction();
  }

  @Bean
  public Action<States, Events> searchDriversAction() {
    return new SearchDriversAction();
  }

  @Bean
  public Action<States, Events> sendDispatchRequestAction() {
    return new SendDispatchRequestAction();
  }

  @Bean
  public Action<States, Events> releaseRequestedDriversAction() {
    return new ReleaseRequestedDriversAction();
  }

  @Bean
  public Action<States, Events> dispatchDeclineAction() {
    return new DispatchDeclineAction();
  }

  @Bean
  @DependsOn("dumpContextAction")
  public Action<States, Events> riderCancelAction() {
    return new RiderCancelAction((ReleaseRequestedDriversAction) releaseRequestedDriversAction());
  }

  @Bean
  @DependsOn("dumpContextAction")
  public Action<States, Events> driverCancelAction() {
    return new DriverCancelAction();
  }

  @Bean
  public Action<States, Events> forceRedispatchAction() {
    return new ForceRedispatchAction();
  }

  @Bean
  @DependsOn("dumpContextAction")
  public Action<States, Events> adminCancelAction() {
    return new AdminCancelAction();
  }

  @Bean
  public Action<States, Events> redispatchOnCancelAction() {
    return new RedispatchOnCancelAction();
  }

  @Bean
  public Action<States,Events> acceptRideAction() {
    return new AcceptRideAction();
  }

  @Bean
  public Action<States, Events> driverReachAction() {
    return new DriverReachAction();
  }

  @Bean
  public Action<States, Events> startRideAction() {
    return new StartRideAction();
  }

  @Bean
  public Action<States, Events> endRideAction() {
    return new EndRideAction();
  }

  @Bean
  public Action<States, Events> updateDestinationAction() {
    return new UpdateDestinationAction();
  }

  @Bean
  public Action<States, Events> updateCommentAction() {
    return new UpdateCommentAction();
  }

  @Bean
  public Action<States, Events> errorHandlingAction() {
    return new ErrorHandlingAction();
  }

  public Action<States, Events> compositeErrorHandlingAction(Action<States, Events>... actions) {
    return new CompositeErrorHandlingAction((ErrorHandlingAction) errorHandlingAction(), actions);
  }

  @Bean
  public Action<States, Events> dispatchNotAcceptedAction() {
    return new DispatchNotAcceptedAction();
  }

  @Bean
  public DefaultConsecutiveDeclineUpdateService consecutiveDeclineUpdateService(ActiveDriverLocationService activeDriverLocationService,
    EventsNotificationService eventsNotificationService, ActiveDriversService activeDriversService, RideDispatchServiceConfig config,
    List<DispatchDeclineRequestChecker> checkers) {
    return new DefaultConsecutiveDeclineUpdateService(activeDriverLocationService, eventsNotificationService, activeDriversService, config, checkers);
  }

  @Bean
  public QueueConsecutiveDeclineUpdateService queueConsecutiveDeclineUpdateService(ActiveDriverLocationService activeDriverLocationService,
    EventsNotificationService eventsNotificationService, ActiveDriversService activeDriversService,
    RideDispatchServiceConfig config, List<DispatchDeclineRequestChecker> checkers, AreaQueueConfig areaQueueConfig,
    AreaQueuePenaltyService penaltyService) {
    return new QueueConsecutiveDeclineUpdateService(activeDriverLocationService, eventsNotificationService,
      activeDriversService, config, checkers, areaQueueConfig, penaltyService);
  }

  @Bean
  public StateMachinePersist<States, Events, String> stateMachinePersist(RedisConnectionFactory connectionFactory) {
    RARedisStateMachineContextRepository repository = new RARedisStateMachineContextRepository(connectionFactory);
    return new RepositoryStateMachinePersist<>(repository);
  }

  @Bean
  public DefaultStateMachinePersister<States, Events, String> redisStateMachinePersister(
    StateMachinePersist<States, Events, String> stateMachinePersist) {
    return new RARedisStateMachinePersister(stateMachinePersist);
  }

  @Bean
  public RideFlowStateMachineListener rideFlowStateMachineListener() {
    return new RideFlowStateMachineListener();
  }

  @Bean
  public RequestedDriversRegistry requestedDriversRegistry(JedisClient jedisClient, Environment environment) {
    return new RequestedDriversRegistry(jedisClient, environment);
  }

  @Bean
  public StackedDriverRegistry stackedDriverRegistry(RedisTemplate redisTemplate, Environment environment) {
    return new StackedDriverRegistry(redisTemplate, environment);
  }

  @Bean
  public ActionAspect actionAspect() {
    return new ActionAspect();
  }

  @Bean
  public InceptionMachinesSubscriber inceptionMachinesSubscriber() {
    return new InceptionMachinesSubscriber(inceptionMachinesTracker());
  }

  @Bean
  public InceptionMachinesTracker inceptionMachinesTracker() {
    return new InceptionMachinesTracker();
  }

  @Bean
  public RedissonClient redissonClient(Environment environment) {
    final Config config = new Config();
    config.useSingleServer()
      .setAddress(String.format("redis://%s:%s", environment.getProperty("cache.redis.host"),
      environment.getProperty("cache.redis.port")));
    return Redisson.create(config);
  }
}
