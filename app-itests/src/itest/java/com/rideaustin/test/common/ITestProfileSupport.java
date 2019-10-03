package com.rideaustin.test.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;

import com.rideaustin.application.cache.impl.JedisClient;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.LocationProvider;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.stubs.quartz.SchedulerService;

@ITestProfile
public abstract class ITestProfileSupport extends AbstractTransactionalJUnit4SpringContextTests {

  @Inject
  protected LocationProvider locationProvider;
  @Inject
  private List<StateVerifier> verifiers;

  @Inject
  protected SchedulerService schedulerService;

  @Inject
  private JedisClient jedisClient;

  @Inject
  protected EntityManager entityManager;

  @Inject
  protected Sleeper sleeper;

  @Inject
  private Flyway flyway;

  @Inject
  private RideDslRepository rideDslRepository;

  @Inject
  private StripeServiceMockImpl stripeServiceMock;

  @Inject
  private EmailCheckerService emailCheckerService;

  @Inject
  private EmailService emailService;

  @Before
  public void setUp() throws Exception {
    flyway.migrate();
    verifiers.forEach(StateVerifier::initialState);
  }

  @After
  public void supportTearDown() {
    schedulerService.discardQueue();
    entityManager.flush();
    jedisClient.flushAll();
    stripeServiceMock.resetFlags();
    emailCheckerService.close();
    ((InterceptingEmailService)emailService).reset();
  }

  protected void sleep(long millis) {
    sleeper.sleep(millis);
  }

  @AfterTransaction
  public void verifyFinalDatabaseState() {
    verifiers.forEach(StateVerifier::verify);
  }

  protected void forceEndRide(Long ride) {
    Ride foundRide = rideDslRepository.findOne(ride);
    foundRide.setEndLocationLat(locationProvider.getCenter().lat);
    foundRide.setEndLocationLong(locationProvider.getCenter().lng);
    rideDslRepository.save(foundRide);
  }

  protected <T extends Avatar> T update(T avatar, Supplier<T> constructor, Consumer<T> operation) {
    T target = constructor.get();
    User targetUser = new User();
    BeanUtils.copyProperties(avatar, target);
    BeanUtils.copyProperties(avatar.getUser(), targetUser);
    target.setUser(targetUser);
    operation.accept(target);
    return target;
  }
}