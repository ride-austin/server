package com.rideaustin.service.eligibility;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessage;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasNoCause;
import static com.rideaustin.service.eligibility.checks.EligibilityCheckItem.Order.FIRST_ORDER;
import static com.rideaustin.service.eligibility.checks.EligibilityCheckItem.Order.ORDER_DOES_NOT_MATTER;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.eligibility.checks.BaseEligibilityCheckItem;

@RunWith(MockitoJUnitRunner.class)
public class EligibilityCheckServiceTest {

  private static final String EXPECTED_ERROR_MESSAGE = "Error";
  private static final long CITY_ID = 1L;
  private static final ImmutableMap<String, Object> CONTEXT = ImmutableMap.of("key", "value");

  @Mock
  private ConfigurationItemCache configurationCache;
  @Mock
  private BeanFactory beanFactory;
  @Mock
  private CurrentUserService currentUserService;
  @InjectMocks
  private TestedDriverEligibilityCheckService testedInstance;

  @Before
  public void setUp() throws Exception {
    when(currentUserService.getUser()).thenReturn(new User());
  }

  @Test
  public void testCheckThrowsServerErrorIfEligibilityConfigurationNotFound() throws Exception {
    // given
    when(configurationCache.getConfigurationForClient(eq(ClientType.DRIVER))).thenReturn(Collections.emptyList());

    // when
    catchException(testedInstance).check(new DriverEligibilityCheckContext(new Driver(), new ActiveDriver(), new Car(), Collections.emptyMap()), CITY_ID);

    // then
    assertThat(caughtException(),
      allOf(
        instanceOf(ServerError.class),
        hasMessage("Can't find eligibility checks for city " + CITY_ID),
        hasNoCause()
      )
    );
  }

  @Test
  public void testCheckUsesRequestedChecksIfProvided() throws Exception {
    // given
    StubDriverEligibilityCheck checkItem = mockCheck(StubDriverEligibilityCheck.class, null);

    // when
    testedInstance.check(
      new DriverEligibilityCheckContext(new Driver(), new ActiveDriver(), new Car(), Collections.emptyMap(), Collections.singleton(StubDriverEligibilityCheck.class)),
      CITY_ID
    );

    // then
    assertTrue(checkItem.isCheckCalled());
  }

  @Test
  public void testCheckLoadsCheckItemsFromConfig() throws Exception {
    // given
    prepareConfigItem(Collections.singleton(StubDriverEligibilityCheck.class.getName()));
    StubDriverEligibilityCheck checkItem = mockCheck(StubDriverEligibilityCheck.class, null);

    // when
    testedInstance.check(
      new DriverEligibilityCheckContext(new Driver(), new ActiveDriver(), new Car(), Collections.emptyMap()),
      CITY_ID
    );

    // then
    assertTrue(checkItem.isCheckCalled());
  }

  @Test
  public void testCheckConvertsEligibilityCheckErrorsToException() throws Exception {
    // given
    prepareConfigItem(Collections.singleton(StubDriverEligibilityCheck.class.getName()));
    StubDriverEligibilityCheck checkItem = mockCheck(StubDriverEligibilityCheck.class, new EligibilityCheckError(EXPECTED_ERROR_MESSAGE));

    // when
    catchException(testedInstance).check(
      new DriverEligibilityCheckContext(new Driver(), new ActiveDriver(), new Car(), Collections.emptyMap()),
      CITY_ID
    );

    // then
    assertTrue(checkItem.isCheckCalled());
    assertThat(caughtException(),
      allOf(
        instanceOf(BadRequestException.class),
        hasMessage(EXPECTED_ERROR_MESSAGE),
        hasNoCause()
      )
    );
  }

  @Test
  public void testCheckFiltersChecksByTargetClass() throws Exception {
    // given
    prepareConfigItem(ImmutableSet.of(StubDriverEligibilityCheck.class.getName(), StubActiveDriverEligibilityCheck.class.getName()));
    StubDriverEligibilityCheck driverCheckItem = mockCheck(StubDriverEligibilityCheck.class, null);
    StubActiveDriverEligibilityCheck activeDriverCheckItem = mockCheck(StubActiveDriverEligibilityCheck.class, null);
    StubCarEligibilityCheck carCheckItem = mockContextCheck(StubCarEligibilityCheck.class, null);

    // when
    testedInstance.check(
      new DriverEligibilityCheckContext(new Driver(), new ActiveDriver(), new Car(), Collections.emptyMap()),
      CITY_ID
    );

    // then
    assertTrue(activeDriverCheckItem.isCheckCalled());
    assertTrue(driverCheckItem.isCheckCalled());
    assertFalse(carCheckItem.isCheckCalled());
  }

  @Test
  public void testCheckPassesContextToContextAwareChecks() throws Exception {
    // given
    prepareConfigItem(Collections.singleton(StubCarEligibilityCheck.class.getName()));
    StubCarEligibilityCheck carCheckItem = mockContextCheck(StubCarEligibilityCheck.class, null);

    // when
    testedInstance.check(
      new DriverEligibilityCheckContext(new Driver(), new ActiveDriver(), new Car(), CONTEXT),
      CITY_ID
    );

    // then
    verify(beanFactory, times(1)).getBean(StubCarEligibilityCheck.class, CONTEXT);
    assertTrue(carCheckItem.isCheckCalled());
  }

  @Test
  public void shouldCallChecksInOrder() throws Exception {
    // given
    prepareConfigItem(ImmutableSet.of(StubZZZFirstCarEligibilityCheck.class.getName(), StubAAALastCarEligibilityCheck.class.getName()));
    StubZZZFirstCarEligibilityCheck carCheckItemFirst = mockContextCheck(StubZZZFirstCarEligibilityCheck.class, new EligibilityCheckError(EXPECTED_ERROR_MESSAGE));
    StubAAALastCarEligibilityCheck carCheckItemLast = mockContextCheck(StubAAALastCarEligibilityCheck.class, new EligibilityCheckError("Last"));

    // when
    catchException(testedInstance).check(
      new DriverEligibilityCheckContext(null, null, new Car(), CONTEXT),
      CITY_ID
    );

    // then
    assertThat(caughtException(),
      allOf(
        instanceOf(BadRequestException.class),
        hasMessage(EXPECTED_ERROR_MESSAGE),
        hasNoCause()
      )
    );
    verify(beanFactory, times(1)).getBean(StubZZZFirstCarEligibilityCheck.class, CONTEXT);
    verify(beanFactory, times(1)).getBean(StubAAALastCarEligibilityCheck.class, CONTEXT);
    assertTrue(carCheckItemFirst.isCheckCalled());
    assertFalse(carCheckItemLast.isCheckCalled());
  }

  private <T> T mockContextCheck(Class<T> clazz, EligibilityCheckError error) throws Exception {
    T checkItem = (T) clazz.getDeclaredConstructors()[0].newInstance(error, Collections.emptyMap());
    when(beanFactory.getBean(eq(clazz), anyVararg())).thenReturn(checkItem);
    return checkItem;
  }

  private <T> T mockCheck(Class<T> clazz, EligibilityCheckError error) throws Exception {
    T checkItem = (T) clazz.getDeclaredConstructors()[0].newInstance(error);
    when(beanFactory.getBean(clazz)).thenReturn(checkItem);
    return checkItem;
  }

  private void prepareConfigItem(Set<String> checkNames) {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setCityId(CITY_ID);
    configurationItem.setConfigurationKey(DriverEligibilityCheckService.DRIVER_ELIGIBILITY);
    configurationItem.setConfigurationObject(ImmutableMap.of(DriverEligibilityCheckService.DRIVER_ELIGIBILITY, checkNames));
    when(configurationCache.getConfigurationForClient(eq(ClientType.DRIVER))).thenReturn(Collections.singletonList(configurationItem));
  }

  @EligibilityCheck(targetClass = Driver.class)
  static class StubDriverEligibilityCheck extends BaseStubEligibilityCheck<Driver> {
    StubDriverEligibilityCheck(EligibilityCheckError error) {
      super(error, Collections.emptyMap());
    }
  }

  @EligibilityCheck(targetClass = ActiveDriver.class)
  static class StubActiveDriverEligibilityCheck extends BaseStubEligibilityCheck<ActiveDriver> {
    StubActiveDriverEligibilityCheck(EligibilityCheckError error) {
      super(error, Collections.emptyMap());
    }
  }

  @EligibilityCheck(targetClass = Car.class, contextAware = true)
  static class StubCarEligibilityCheck extends BaseStubEligibilityCheck<Car> {
    StubCarEligibilityCheck(EligibilityCheckError error, Map<String, Object> context) {
      super(error, context);
    }
  }

  @EligibilityCheck(targetClass = Car.class, contextAware = true)
  static class StubZZZFirstCarEligibilityCheck extends BaseStubEligibilityCheck<Car> {
    StubZZZFirstCarEligibilityCheck(EligibilityCheckError error, Map<String, Object> context) {
      super(error, context);
    }

    @Override
    public int getOrder() {
      return FIRST_ORDER;
    }
  }

  @EligibilityCheck(targetClass = Car.class, contextAware = true)
  static class StubAAALastCarEligibilityCheck extends BaseStubEligibilityCheck<Car> {
    StubAAALastCarEligibilityCheck(EligibilityCheckError error, Map<String, Object> context) {
      super(error, context);
    }

    @Override
    public int getOrder() {
      return ORDER_DOES_NOT_MATTER;
    }
  }

  static abstract class BaseStubEligibilityCheck<T> extends BaseEligibilityCheckItem<T> {
    private final EligibilityCheckError error;
    private boolean checkCalled = false;

    protected BaseStubEligibilityCheck(EligibilityCheckError error, Map<String, Object> context) {
      super(context);
      this.error = error;
    }

    @Override
    public Optional<EligibilityCheckError> check(T subject) {
      checkCalled = true;
      return Optional.ofNullable(error);
    }

    public boolean isCheckCalled() {
      return checkCalled;
    }
  }

  private static class TestedDriverEligibilityCheckService extends DriverEligibilityCheckService {

    public TestedDriverEligibilityCheckService(ConfigurationItemCache configurationCache, BeanFactory beanFactory, CurrentUserService currentUserService) {
      super(configurationCache, beanFactory, currentUserService);
    }

    @Override
    protected Set<Class<?>> getRegisteredChecks() {
      return ImmutableSet.of(
        StubDriverEligibilityCheck.class, StubActiveDriverEligibilityCheck.class, StubCarEligibilityCheck.class,
        StubZZZFirstCarEligibilityCheck.class, StubAAALastCarEligibilityCheck.class);
    }
  }
}