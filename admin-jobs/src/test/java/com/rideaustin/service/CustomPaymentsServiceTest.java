package com.rideaustin.service;

import static com.rideaustin.test.util.TestUtils.RANDOM;
import static com.rideaustin.test.util.TestUtils.setupCurrentUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CustomPaymentCategory;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CustomPaymentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;

@RunWith(MockitoJUnitRunner.class)
public class CustomPaymentsServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private CustomPaymentDslRepository customPaymentDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private DriverService driverService;
  @Mock
  private AdministratorService administratorService;
  @Mock
  private DriverDslRepository driverDslRepository;

  private CustomPaymentService customPaymentService;

  private Driver driver;
  private Administrator administrator;
  private User administratorUser;

  private List<CustomPayment> customPaymentList = new ArrayList<>();

  @Before
  public void setup() throws Exception {
    User driverUser = setupCurrentUser(currentUserService, AvatarType.DRIVER);
    administratorUser = setupCurrentUser(currentUserService, AvatarType.ADMIN);
    driver = new Driver();
    driver.setUser(driverUser);
    driver.getUser().setId(RANDOM.nextLong());

    administrator = new Administrator();
    administrator.setUser(administratorUser);
    administrator.getUser().setId(RANDOM.nextLong());

    fillCustomPaymentList(20);

    customPaymentService = new CustomPaymentService(customPaymentDslRepository,
      currentUserService, driverService, administratorService, driverDslRepository);
  }

  @Test
  public void testCreateOtherPaymentOK() throws RideAustinException {
    when(currentUserService.getUser()).thenReturn(administratorUser);
    when(administratorService.findAdministrator(any())).thenReturn(administrator);
    when(driverService.findDriver(anyLong())).thenReturn(driver);
    when(customPaymentDslRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

    CustomPayment cp = customPaymentService.createOtherPayment(CustomPaymentCategory.BONUS, "test", 1L, 20d, new Date());
    assertThat(cp.getDriver(), is(driver));
    assertThat(cp.getValue().getAmount().doubleValue(), is(20d));
  }

  @Test
  public void testCreateOtherPaymentNotADriver() throws RideAustinException {
    when(currentUserService.getUser()).thenReturn(administratorUser);
    when(administratorService.findAdministrator(any())).thenReturn(administrator);
    when(driverService.findDriver(anyLong())).thenThrow(new NotFoundException("Driver not found"));
    when(customPaymentDslRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

    expectedException.expect(NotFoundException.class);
    customPaymentService.createOtherPayment(CustomPaymentCategory.BONUS, "test", 1L, 20d, new Date());
  }

  @Test
  public void testCreateOtherPaymentNotAAdministrator() throws RideAustinException {
    when(currentUserService.getUser()).thenReturn(administratorUser);
    when(administratorService.findAdministrator(any())).thenThrow(new NotFoundException("Administrator not found"));
    when(driverService.findDriver(anyLong())).thenReturn(driver);
    when(customPaymentDslRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

    expectedException.expect(NotFoundException.class);
    customPaymentService.createOtherPayment(CustomPaymentCategory.BONUS, "test", 1L, 20d, new Date());
  }

  @Test
  public void testGetWeeklyCustomPaymentsForDriver() throws RideAustinException {
    // preconditions
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);
    when(customPaymentDslRepository.findForDriverBetweenDates(any(), any(), any())).thenReturn(customPaymentList);
    // test
    List<CustomPayment> customPayments = customPaymentService.getWeeklyCustomPaymentsForDriver(LocalDate.now(),LocalDate.now(), 1L);
    // verify
    assertThat(customPayments, is(notNullValue()));
    assertThat(customPayments.size(), is(20));
  }

  private void fillCustomPaymentList(int count) {
    for (int i = 0; i < count; i++) {
      customPaymentList.add(creteSampleCustomPayment());
    }
  }

  private CustomPayment creteSampleCustomPayment() {
    CustomPayment cp = new CustomPayment();
    Random random = new Random();
    cp.setCreator(administrator);
    cp.setDriver(driver);
    cp.setValue(Money.of(CurrencyUnit.USD, random.nextInt(10000) / 100));
    cp.setCreatedDate(new Date());
    cp.setUpdatedDate(new Date());
    cp.setId(random.nextLong());
    return cp;
  }
}
