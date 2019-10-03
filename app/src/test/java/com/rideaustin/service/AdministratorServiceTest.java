package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static com.rideaustin.test.util.TestUtils.RANDOM;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.AdministratorDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;

@RunWith(MockitoJUnitRunner.class)
public class AdministratorServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private AdministratorDslRepository administratorDslRepository;

  private AdministratorService administratorService;

  private Administrator administrator;
  private User administratorUser;

  @Before
  public void setup() throws Exception {

    administratorUser = new User();
    administratorUser.setId(1L);

    administrator = new Administrator();
    administrator.setUser(administratorUser);
    administrator.getUser().setId(RANDOM.nextLong());

    administratorService = new AdministratorService(administratorDslRepository);
  }

  @Test
  public void testFindAdministrator() throws RideAustinException {
    when(administratorDslRepository.findById(any())).thenReturn(administrator);
    Administrator admin = administratorService.findAdministrator(1L);
    assertThat(admin, is(administrator));
  }

  @Test
  public void testFindAdministratorNotFound() throws RideAustinException {
    when(administratorDslRepository.findById(any())).thenReturn(null);
    expectedException.expect(NotFoundException.class);
    administratorService.findAdministrator(1L);
  }
  @Test
  public void testFindAdministratorByUser() throws RideAustinException {
    when(administratorDslRepository.findByUser(any())).thenReturn(administrator);
    Administrator admin = administratorService.findAdministrator(administratorUser);
    assertThat(admin, is(administrator));
  }

  @Test
  public void testFindAdministratorByUserNotFound() throws RideAustinException {
    when(administratorDslRepository.findByUser(any())).thenReturn(null);
    expectedException.expect(NotFoundException.class);
    administratorService.findAdministrator(administratorUser);
  }
/*
  @Test
  public void testCreateOtherPaymentNotADriver() throws RideAustinException {
    when(currentUserService.getUser()).thenReturn(administratorUser);
    when(administratorService.findAdministrator(any())).thenReturn(administrator);
    when(driverService.findDriver(anyLong())).thenReturn(null);
    when(customPaymentDslRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

    expectedException.expect(BadRequestException.class);
    customPaymentService.createOtherPayment(CustomPaymentCategory.BONUS, "test", 1L, 20d);
  }

  @Test
  public void testCreateOtherPaymentNotAAdministrator() throws RideAustinException {
    when(currentUserService.getUser()).thenReturn(administratorUser);
    when(administratorService.findAdministrator(any())).thenReturn(null);
    when(driverService.findDriver(anyLong())).thenReturn(driver);
    when(customPaymentDslRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

    expectedException.expect(ServerError.class);
    customPaymentService.createOtherPayment(CustomPaymentCategory.BONUS, "test", 1L, 20d);
  }

  @Test
  public void testCollectWeeklyCustomPaymentsForDriver() throws RideAustinException {
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);
    when(customPaymentDslRepository.findForDriverBetweenDates(any(), any(), any())).thenReturn(customPaymentList);
    Map<Driver, List<CustomPayment>> cpm = customPaymentService.collectWeeklyCustomPayments(LocalDate.now(), 1l);
    assertThat(cpm.size(), is(1));
    assertThat(cpm.get(driver).size(), is(20));
  }

  @Test
  public void testCollectWeeklyCustomPaymentsForAllDrivers() throws RideAustinException {
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);
    when(customPaymentDslRepository.findBetweenDates(any(), any())).thenReturn(customPaymentList);
    Map<Driver, List<CustomPayment>> cpm = customPaymentService.collectWeeklyCustomPayments(LocalDate.now(), null);
    assertThat(cpm.size(), is(1));
    assertThat(cpm.get(driver).size(), is(20));
  }
  */

}
