package com.rideaustin.service;

import static com.rideaustin.Constants.ErrorMessages.TERM_NOT_FOUND_TEMPLATE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
import com.rideaustin.model.Terms;
import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.model.TermsInfo;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.TermsDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.exception.RideAustinException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TermsService {

  private final TermsDslRepository termsDslRepository;

  public TermsAcceptance getDriverCurrentAcceptance(Long driverId, Long cityId) {
    return termsDslRepository.getCurrentTermAcceptance(driverId, cityId);
  }

  public TermsInfo getCurrentTerms(Long cityId) {
    return termsDslRepository.getCurrent(cityId);
  }

  public Map<Long, TermsAcceptance> getDriversCurrentAcceptance(List<Long> driverIds, Long cityId) {
    List<TermsAcceptance> ta = termsDslRepository.getCurrentTermAcceptances(driverIds, cityId);
    Map<Long, TermsAcceptance> acceptances = new HashMap<>();
    if (CollectionUtils.isNotEmpty(ta)) {
      ta.forEach(t -> acceptances.put(t.getDriver().getId(), t));
    }
    return acceptances;
  }

  public TermsAcceptance acceptTerms(@Nonnull Driver driver, @Nonnull Long termId) throws RideAustinException {
    Terms terms = termsDslRepository.getOne(termId);
    if (terms == null) {
      throw new ConflictException(String.format(TERM_NOT_FOUND_TEMPLATE, termId));
    }

    checkForExistingTermAcceptances(driver.getId(), termId);
    TermsAcceptance ta = TermsAcceptance.builder().driver(driver).terms(terms).build();
    ta.setCreatedDate(new Date());
    ta.setUpdatedDate(new Date());
    termsDslRepository.save(ta);
    return ta;
  }

  private void checkForExistingTermAcceptances(Long driverId, Long termId) throws BadRequestException {
    TermsAcceptance termsAcceptance = termsDslRepository.getTermsAcceptance(driverId, termId);
    if(termsAcceptance != null){
      throw new BadRequestException(Constants.ErrorMessages.TERM_DRIVER_ACCEPTED_ALREADY);
    }
  }
}
