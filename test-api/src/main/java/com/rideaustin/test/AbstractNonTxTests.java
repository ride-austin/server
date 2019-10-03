package com.rideaustin.test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.awaitility.Awaitility;
import com.rideaustin.Constants;
import com.rideaustin.application.cache.impl.JedisClient;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.setup.SetupAction;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.stubs.quartz.SchedulerService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

@ITestProfile
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractNonTxTests<T extends SetupAction<T>> extends AbstractJUnit4SpringContextTests {

  @Inject
  private Flyway flyway;

  @Inject
  protected Sleeper sleeper;

  @Inject
  protected SchedulerService schedulerService;

  private static JedisClient jedisClient;

  @Inject
  protected StripeServiceMockImpl stripeServiceMock;

  @Inject
  private EmailCheckerService emailCheckerService;

  @Inject
  private EmailService emailService;

  @Inject
  protected T setup;

  @Inject
  protected StateMachinePersist<States, Events, String> contextAccess;

  @Inject
  protected RideDslRepository rideDslRepository;

  @Inject
  protected LocationProvider locationProvider;

  @Inject
  protected Environment environment;

  @Inject
  protected ConfigurationItemCache configurationItemCache;

  protected static JdbcTemplate JDBC_TEMPLATE;

  @Before
  public void setUp() throws Exception {
    flyway.migrate();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "driverStats", "enabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "rideMessaging", "enabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "queue", "penaltyEnabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "upfrontEnabled", false);
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "stackedRides", "dropoffExpectationTime", 60);
  }

  protected T createSetup() throws InterruptedException, ExecutionException {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    return executorService.submit(() -> setup.setUp()).get();
  }

  protected void execute(Runnable action) {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    executorService.execute(action);
  }

  @AfterClass
  public static void dropDatabase() {
    JDBC_TEMPLATE.execute("set foreign_key_checks=0;");
    String[] truncated = new String[]{"active_drivers", "administrators","avatar_documents","avatar_email_notifications",
      "avatars", "car_documents", "cars", "documents","drivers", "drivers_aud",
      "driver_statistics","events", "fare_payments", "geo_log", "lost_and_found_requests", "phone_verification_items",
      "promocode_redemptions", "promocodes", "rating_updates", "ride_calls", "ride_driver_dispatches", "rider_card_locks",
      "rider_cards", "riders", "rides", "ride_trackers", "ride_upgrade_requests", "sessions","surge_areas",
      "surge_areas_history", "surge_factors","terms_acceptance","tokens", "users", "api_clients",
      "queued_rides","area_queue","events"
    };
    for (String table : truncated) {
      JDBC_TEMPLATE.execute(String.format("TRUNCATE TABLE `%s`;", table));
    }
    JDBC_TEMPLATE.execute("set foreign_key_checks=1;");
    jedisClient.flushAll();
  }

  @After
  public void supportTearDown() {
    schedulerService.discardQueue();
    jedisClient.flushAll();
    stripeServiceMock.resetFlags();
    emailCheckerService.close();
    ((InterceptingEmailService) emailService).reset();
  }

  protected void forceEndRide(Long ride) {
    execute(() -> {
      Ride foundRide = rideDslRepository.findOne(ride);
      foundRide.setEndLocationLat(locationProvider.getCenter().lat);
      foundRide.setEndLocationLong(locationProvider.getCenter().lng);
      rideDslRepository.save(foundRide);
    });
  }

  @Inject
  public void setDataSource(DataSource dataSource) {
    JDBC_TEMPLATE = new JdbcTemplate(dataSource);
  }

  @Inject
  public void setJedisClient(JedisClient jedisClient) {
    AbstractNonTxTests.jedisClient = jedisClient;
  }

  protected void awaitDispatch(ActiveDriver driver, Long ride) {
    awaitDispatch(driver, ride, 20, environment, contextAccess);
  }

  public static void awaitDispatch(ActiveDriver driver, Long ride, long dispatchTimeout, Environment environment, StateMachinePersist<States, Events, String> contextAccess) {
    Awaitility.await()
      .atMost(dispatchTimeout, TimeUnit.SECONDS)
      .until(() -> {
        Optional<StateMachineContext<States, Events>> persistedContext = Optional.ofNullable(StateMachineUtils.getPersistedContext(environment, contextAccess, ride));
        boolean driverAssigned = persistedContext
          .map(StateMachineContext::getExtendedState)
          .map(StateMachineUtils::getDispatchContext)
          .map(DispatchContext::getCandidate)
          .map(DispatchCandidate::getId)
          .map(id -> id.equals(driver.getId()))
          .orElse(false);
        boolean state = persistedContext.map(StateMachineContext::getState).map(States.DISPATCH_PENDING::equals).orElse(false);
        return state && driverAssigned;
      });
  }

  protected void awaitState(Long rideId, States... expectedState) {
    Awaitility.await()
      .atMost(90, TimeUnit.SECONDS)
      .until(() -> {
        Optional<StateMachineContext<States, Events>> persistedContext = Optional.ofNullable(StateMachineUtils.getPersistedContext(environment, contextAccess, rideId));
        return persistedContext
          .map(StateMachineContext::getState)
          .map(s -> Arrays.asList(expectedState).contains(s))
          .orElse(Arrays.asList(expectedState).contains(States.ENDED));
      });
  }

  protected void awaitStatus(Long ride, RideStatus status) {
    Awaitility.await()
      .atMost(90, TimeUnit.SECONDS)
      .until(() -> {
        Ride found = rideDslRepository.findOne(ride);
        return status.equals(found.getStatus());
      });
  }
}
