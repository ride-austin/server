package com.rideaustin.test.config;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.model.RidePushNotificationRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.DriverAuditedService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.service.email.monitor.SimpleEmailCheckerService;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.notifications.PushNotificationsService;
import com.rideaustin.service.payment.PaymentEmailService;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.test.LocationProvider;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.actions.ApiClientAction;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.UserAction;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.common.AwaitilitySleeper;
import com.rideaustin.test.common.DatabaseStateVerifier;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.common.StateVerifier;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.stubs.DriverDslRepository;
import com.rideaustin.test.stubs.NotificationFacade;
import com.rideaustin.test.stubs.S3StorageService;
import com.rideaustin.test.stubs.quartz.SchedulerService;
import com.rideaustin.test.stubs.transactional.PaymentJobService;
import com.rideaustin.test.stubs.transactional.PaymentService;

@Configuration
@Import(CacheConfiguration.class)
public class TestActionsConfig {

  @Bean
  public DriverAction driverAction(WebApplicationContext context, ObjectMapper mapper) {
    return new DriverAction(webAppContextSetup(context).apply(springSecurity()).build(), mapper);
  }

  @Bean
  public RiderAction riderAction(WebApplicationContext context, ObjectMapper mapper) {
    return new RiderAction(webAppContextSetup(context).apply(springSecurity()).build(), mapper);
  }

  @Bean
  public ApiClientAction apiClientAction(WebApplicationContext context, ObjectMapper mapper) {
    return new ApiClientAction(webAppContextSetup(context).apply(springSecurity()).build(), mapper);
  }

  @Bean
  public UserAction userAction(WebApplicationContext context, ObjectMapper mapper) {
    return new UserAction(webAppContextSetup(context).apply(springSecurity()).build(), mapper);
  }

  @Bean
  public AdministratorAction administratorAction(WebApplicationContext context, ObjectMapper mapper) {
    return new AdministratorAction(webAppContextSetup(context).apply(springSecurity()).build(), mapper);
  }

  @Bean
  public RideAction rideAction(RiderAction riderAction, DriverAction driverAction, Sleeper sleeper, StateMachinePersist<States, Events, String> contextAccess, Environment environment) {
    return new RideAction(riderAction, driverAction, sleeper, contextAccess, environment);
  }

  @Bean
  @Profile("itest")
  public SchedulerService schedulerService(ApplicationContext context) {
    return new SchedulerService(context);
  }

  @Bean
  @Profile("itest")
  public AreaQueueConfig areaQueueConfig(ConfigurationItemCache configurationItemCache) {
    return new com.rideaustin.test.stubs.AreaQueueConfig(configurationItemCache);
  }

  @Bean
  public EventAssertHelper eventAssertHelper() {
    return new EventAssertHelper();
  }

  @Bean
  @Profile("itest")
  public NotificationFacade notificationsFacade(TokenRepository tokenRepository, PushNotificationsService notificationsService,
    CityCache cityCache, Environment environment, RidePushNotificationRepository repository) {
    return new NotificationFacade(tokenRepository, notificationsService, cityCache, environment, repository);
  }

  @Bean
  @Profile("itest")
  public PaymentService paymentService(CurrentSessionService currentSessionService, StripeService stripeService, RideDslRepository rideDslRepository,
    PaymentEmailService paymentEmailService, RiderDslRepository riderDslRepository, PromocodeService promocodeService,
    FarePaymentService farePaymentService, FareService fareService, RiderCardService riderCardService,
    RideLoadService rideLoadService, PushNotificationsFacade pushNotificationsFacade, CampaignService campaignService,
    RidePaymentConfig config) {
    return new PaymentService(currentSessionService, stripeService, rideDslRepository, paymentEmailService,
      riderDslRepository, promocodeService, farePaymentService, fareService, riderCardService, rideLoadService,
      pushNotificationsFacade, campaignService, config);
  }

  @Bean
  @Profile("itest")
  public PaymentJobService paymentJobService(SchedulerService schedulerService) {
    return new PaymentJobService(schedulerService);
  }

  @Bean
  @Profile("itest")
  public ConfigurationItemCache configurationItemCache(ConfigurationItemService configurationItemService, ObjectMapper objectMapper) {
    return new ConfigurationItemCache(configurationItemService, objectMapper);
  }

  @Bean
  @Profile("itest")
  public DriverDslRepository driverDslRepository(DriverAuditedService driverAuditedService) {
    return new DriverDslRepository(driverAuditedService);
  }

  @Bean
  @Profile("itest")
  public S3StorageService s3StorageService(Environment environment) {
    return new S3StorageService(environment);
  }

  @Bean
  @Scope("prototype")
  public EmailCheckerService checkerService(EmailService emailService) {
    return new SimpleEmailCheckerService(emailService);
  }

  @Bean
  public LocationProvider locationProvider(AreaService areaService) {
    return new LocationProvider(areaService);
  }

  @Bean
  public Sleeper sleeper() {
    return new AwaitilitySleeper();
  }

  @Bean
  @Scope("prototype")
  public StateVerifier databaseStateVerifier(JdbcTemplate jdbcTemplate) {
    return new DatabaseStateVerifier(jdbcTemplate);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource){
    return new JdbcTemplate(dataSource);
  }
}
