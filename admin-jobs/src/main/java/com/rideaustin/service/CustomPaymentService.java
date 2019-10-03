package com.rideaustin.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.enums.CustomPaymentCategory;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CustomPaymentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListCustomPaymentsParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CustomPaymentService {

  private final CustomPaymentDslRepository customPaymentDslRepository;
  private final CurrentUserService currentUserService;
  private final DriverService driverService;
  private final AdministratorService administratorService;
  private final DriverDslRepository driverDslRepository;

  public CustomPayment createOtherPayment(@Nonnull CustomPaymentCategory category,
    @Nonnull String description,
    @Nonnull Long driverId,
    @Nonnull Double value,
    Date paymentDate
  ) throws RideAustinException {
    User currentUser = currentUserService.getUser();
    Administrator administrator = administratorService.findAdministrator(currentUser);

    Driver driver = driverService.findDriver(driverId);
    CustomPayment customPayment = new CustomPayment();
    customPayment.setDriver(driver);
    customPayment.setCategory(category);
    customPayment.setValue(Money.of(CurrencyUnit.USD, value));
    customPayment.setCreatedDate(new Date());
    customPayment.setCreator(administrator);
    customPayment.setUpdatedDate(new Date());
    customPayment.setDescription(description);
    customPayment.setPaymentDate(paymentDate);
    return customPaymentDslRepository.save(customPayment);
  }

  /**
   * Weekly custom payment for specified driver.
   *
   * @param reportDate
   * @param previousMonday
   * @param driverId
   * @return
   */
  public List<CustomPayment> getWeeklyCustomPaymentsForDriver(LocalDate reportDate, LocalDate previousMonday, Long driverId) {
    Date startDate = DateUtils.localDateTimeToDate(previousMonday.atStartOfDay(), Constants.CST_ZONE);
    Date endDate = DateUtils.localDateTimeToDate(reportDate.atStartOfDay().plusDays(1).plusSeconds(-1), Constants.CST_ZONE);

    Driver driver = driverDslRepository.findById(driverId);

    return customPaymentDslRepository.findForDriverBetweenDates(driver, startDate, endDate);
  }

  public Page<CustomPayment> listOtherPayments(ListCustomPaymentsParams params, PagingParams paging) {
    return customPaymentDslRepository.findOtherPayments(params.fill(), paging);
  }

  public CustomPayment findById(@Nonnull Long id) {
    return customPaymentDslRepository.findById(id);
  }

}
