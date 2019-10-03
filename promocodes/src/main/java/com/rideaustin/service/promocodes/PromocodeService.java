package com.rideaustin.service.promocodes;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.promocodes.PromocodeType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.PromocodeDslRepository;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.exception.UnAuthorizedException;
import com.rideaustin.rest.model.ListPromocodeDto;
import com.rideaustin.rest.model.ListPromocodeParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.PromocodeRedemptionDTO;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.utils.RandomString;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PromocodeService {

  public static final String INVALID_PROMOCODE = "Sorry, invalid promocode";

  private final PromocodeDslRepository promocodeDslRepository;
  private final PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;
  private final RiderDslRepository riderDslRepository;
  private final CurrentUserService currentUserService;
  private final TimeService timeService;
  private final RideDslRepository rideDslRepository;
  private final BeanFactory beanFactory;
  private final PromocodeServiceConfig promocodeServiceConfig;

  private final CityCache cityCache;
  private final CarTypesCache carTypesCache;
  private final ObjectMapper objectMapper;

  public Page<ListPromocodeDto> listPromocodes(ListPromocodeParams searchCriteria, PagingParams paging) {
    return promocodeDslRepository.findPromocodes(searchCriteria, paging);
  }

  public Promocode getPromocode(Long promocodeId) {
    return promocodeDslRepository.findOne(promocodeId);
  }

  public long getUsageCount(long promocodeId) {
    return safeZero(promocodeDslRepository.findUsageCount(promocodeId));
  }

  public Promocode getRiderPromocode(Long riderId) throws BadRequestException {
    Promocode promocode = promocodeDslRepository.findPromocodeByRider(riderId);
    if (promocode == null) {
      throw new BadRequestException(INVALID_PROMOCODE);
    }
    return promocode;
  }

  public Promocode addPromocode(Promocode promocode) throws RideAustinException {
    User user = currentUserService.getUser();
    validatePromocode(promocode);
    Promocode newPromocode = new Promocode();
    populatePromocodeData(newPromocode, promocode);
    try {
      newPromocode = promocodeDslRepository.save(newPromocode);
      log.info(String.format("User: %s added new promocode %s", user.getEmail(), objectMapper.writeValueAsString(newPromocode)));
      return newPromocode;
    } catch (PersistenceException e) {
      log.error("Failed to persist code", e);
      throw new BadRequestException("Non unique code literal");
    } catch (JsonProcessingException e) {
      throw new ServerError("Unable to log actions", e);
    }
  }

  public Promocode updatePromocode(Long promocodeId, Promocode updatePromocode) throws RideAustinException {
    User user = currentUserService.getUser();
    Promocode existing = promocodeDslRepository.findOne(promocodeId);
    Promocode existingByLiteral = promocodeDslRepository.findByLiteral(updatePromocode.getCodeLiteral());
    if (existingByLiteral != null && !existing.equals(existingByLiteral)) {
      throw new BadRequestException(String.format("Code literal %s is already in use", updatePromocode.getCodeLiteral()));
    }
    populatePromocodeData(existing, updatePromocode);
    existing = promocodeDslRepository.save(existing);
    try {
      log.info("User: " + user.getEmail() + " updated promocode " + objectMapper.writeValueAsString(existing));
    } catch (JsonProcessingException e) {
      throw new ServerError("Unable to log actions", e);
    }
    return existing;
  }

  public Promocode applyPromocode(Long riderId, String promocodeLiteral) throws RideAustinException {
    return applyPromocode(riderId, promocodeLiteral, true);
  }

  public Promocode applyPromocode(Long riderId, String promocodeLiteral, boolean doAuthCheck) throws RideAustinException {
    Rider rider = getRider(riderId);
    if (doAuthCheck) {
      checkIfAuthenticated(rider);
    }

    Promocode promocode = getPromocode(promocodeLiteral);
    checkIfCanBeUsed(promocode);
    checkIfRiderEligible(promocode, rider);
    checkIfMaximumExceeded(promocode, rider);
    try {
      getPromocodePolicy(promocode).applyPromocode(rider, promocode);
    } catch (PromocodeException e) {
      log.error("Error applying promocode", e);
      throw new BadRequestException(e.getMessage());
    }
    return updatePromocode(promocode);
  }

  public void assignRiderPromocode(Rider owner) {
    Promocode promocode = new Promocode();
    promocode.setOwner(owner);
    promocode.setPromocodeType(PromocodeType.USER);
    promocode.setCodeLiteral(RandomString.generate(8));
    promocode.setCodeValue(promocodeServiceConfig.getUserPromocodeFreeCreditAmount());
    promocode.setMaximumRedemption(promocodeServiceConfig.getUserPromocodeMaxRedemption());
    promocode.setMaximumUsesPerAccount(1);
    promocode.setValidForNumberOfDays(promocodeServiceConfig.getUserPromoCodesValidityPeriod());
    promocode.setCityBitmask(cityCache.toBitMask(ImmutableList.of(owner.getCityId())));
    promocodeDslRepository.save(promocode);
  }

  public void deactivateExpiredRedemptions() {
    List<PromocodeRedemption> expired = promocodeRedemptionDslRepository.findExpiredRedemptions(timeService.getCurrentDate());
    expired.forEach(r -> r.setActive(false));
    promocodeRedemptionDslRepository.saveMany(expired);
  }

  public PromocodeUseResult usePromocode(PromocodeUseRequest request) {
    return usePromocode(request, false);
  }

  public PromocodeUseResult usePromocode(PromocodeUseRequest request, boolean dryRun) {
    if (request.getRedemptionId() != null) {
      Ride ride = rideDslRepository.findOne(request.getRideId());
      PromocodeRedemption redemption = promocodeRedemptionDslRepository.findOne(request.getRedemptionId());
      return new PromocodeUseResult(true, ride.getFreeCreditCharged().getAmount(), redemption);
    }
    PromocodeUseResult defaultResult = new PromocodeUseResult();
    if (!request.isValid() || request.isCoveredByCampaign()) {
      return defaultResult;
    }
    Optional<PromocodeRedemption> eligibleRedemption = getEligibleRedemption(request);

    return eligibleRedemption.map(promocodeRedemption -> getPromocodePolicy(promocodeRedemption.getPromocode())
      .dryRun(dryRun)
      .useRedemption(request, promocodeRedemption))
      .orElse(defaultResult);
  }

  public BigDecimal getTotalCreditForRider(Long riderId) throws ForbiddenException {
    Rider rider = checkAccess(riderId);
    return promocodeRedemptionDslRepository.getRemainingSumForRider(riderId, cityCache.getCity(rider.getCityId()));
  }

  public List<PromocodeRedemptionDTO> getRedemptionsForRider(Long riderId) throws ForbiddenException {
    Rider rider = checkAccess(riderId);
    return promocodeRedemptionDslRepository.getRedemptionsInfo(riderId, cityCache.getCity(rider.getCityId()));
  }

  private Rider checkAccess(Long riderId) throws ForbiddenException {
    User user = currentUserService.getUser();
    Rider rider = user.getAvatar(Rider.class);
    if (!user.isAdmin() && rider.getId() != riderId) {
      throw new ForbiddenException();
    }
    if (user.isAdmin()) {
      rider = riderDslRepository.getRider(riderId);
    }
    return rider;
  }

  private Optional<PromocodeRedemption> getEligibleRedemption(PromocodeUseRequest request) {
    List<PromocodeRedemption> activeRedemptions = getActiveRedemptions(request.getRiderId());
    activeRedemptions = updateActivity(activeRedemptions);
    List<PromocodeRedemption> eligibleRedemptions = filterEligible(activeRedemptions, request);
    return findNextEligibleRedemption(eligibleRedemptions);
  }

  private void checkIfMaximumExceeded(Promocode promocode, Rider rider) throws BadRequestException {
    BigDecimal activeRiderCredit = getAvailableFreeCreditForRider(rider);
    if (activeRiderCredit.add(promocode.getCodeValue()).compareTo(promocodeServiceConfig.getMaximumActiveRedeemedCredit()) > 0) {
      throw new BadRequestException(String.format("Maximum allowed credit ($%s) exceeded", promocodeServiceConfig.getMaximumActiveRedeemedCredit().toString()));
    }
  }

  private Rider getRider(Long riderId) throws BadRequestException {
    Rider rider = riderDslRepository.getRider(riderId);
    if (rider == null) {
      throw new BadRequestException("Rider does not exist");
    }
    return rider;
  }

  private PromocodeRedemptionPolicy getPromocodePolicy(Promocode promocode) {
    return beanFactory.getBean(promocode.getPromocodeType().getFreeCreditPolicy());
  }

  private Promocode updatePromocode(Promocode promocode) {
    promocode.increaseCurrentRedemption();
    return promocodeDslRepository.save(promocode);
  }

  private void checkIfRiderEligible(Promocode promocode, Rider rider) throws BadRequestException {
    if (promocode.isNewRidersOnly()) {
      if (getRedemptionsCount(rider) > 0) {
        throw new BadRequestException("This promocode is for new riders only");
      }
      if (rideDslRepository.countCompletedRidesPerRider(rider) > 0) {
        throw new BadRequestException("This promocode is for new riders only");
      }
    }
    if (promocodeRedemptionDslRepository.findPromocodeRedemption(promocode, rider) != null) {
      throw new BadRequestException("This promocode has already been used");
    }
    if (promocode.getOwner() != null && rider.getId() == promocode.getOwner().getId()) {
      throw new BadRequestException("You cannot redeem your own promocode");
    }
  }

  private void checkIfCanBeUsed(Promocode promocode) throws BadRequestException {
    if (promocode.getMaximumRedemption() != null
      && promocode.getMaximumRedemption() <= safeZero(promocode.getCurrentRedemption())) {
      throw new BadRequestException(INVALID_PROMOCODE);
    }
    if (promocode.getStartsOn() != null && promocode.getStartsOn().compareTo(timeService.getCurrentDate()) > 0) {
      throw new BadRequestException(INVALID_PROMOCODE);
    }
    if (promocode.getEndsOn() != null && promocode.getEndsOn().compareTo(timeService.getCurrentDate()) < 0) {
      throw new BadRequestException(INVALID_PROMOCODE);
    }
    if (promocode.getUseEndDate() != null && promocode.getUseEndDate().compareTo(timeService.getCurrentDate()) < 0) {
      throw new BadRequestException(INVALID_PROMOCODE);
    }
  }

  private Promocode getPromocode(String promocodeLiteral) throws BadRequestException {
    Promocode promocode = promocodeDslRepository.findByLiteral(promocodeLiteral);
    if (promocode == null) {
      throw new BadRequestException(INVALID_PROMOCODE);
    }
    return promocode;
  }

  private void checkIfAuthenticated(Rider rider) throws UnAuthorizedException {
    if (!rider.getUser().equals(currentUserService.getUser())) {
      throw new UnAuthorizedException("Rider not authorized");
    }
  }

  private void populatePromocodeData(Promocode existing, Promocode updatedPromocode) {
    BeanUtils.copyProperties(updatedPromocode, existing, "id", "createdDate", "updatedDate");
  }

  private void validatePromocode(Promocode promocode) throws BadRequestException {
    Promocode possibleDuplicated = promocodeDslRepository.findByLiteral(promocode.getCodeLiteral());
    if (possibleDuplicated != null) {
      throw new BadRequestException("Code literal already exists.");
    }
  }

  private BigDecimal getAvailableFreeCreditForRider(Rider rider) {
    return getActiveRedemptions(rider.getId())
      .stream().map(PromocodeRedemption::getRemainingValue).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Optional<PromocodeRedemption> findNextEligibleRedemption(List<PromocodeRedemption> eligibleRedemptions) {
    //null in valid until means unlimited validity comparator order in correct way
    orderRedemptions(eligibleRedemptions);
    // we are using next trip only first
    Optional<PromocodeRedemption> eligibleRedemption
      = eligibleRedemptions.stream().filter(r -> r.getPromocode().isNextTripOnly()).findFirst();
    if (eligibleRedemption.isPresent()) {
      return eligibleRedemption;
    }
    // next the codes that we started but con consumed fully
    eligibleRedemption
      = eligibleRedemptions.stream().filter(r -> r.getOriginalValue().compareTo(r.getRemainingValue()) != 0).findFirst();
    if (eligibleRedemption.isPresent()) {
      return eligibleRedemption;
    }
    eligibleRedemption = eligibleRedemptions.stream().findFirst();
    return eligibleRedemption;

  }

  private List<PromocodeRedemption> updateActivity(List<PromocodeRedemption> activeRedemptions) {
    Date currentDate = timeService.getCurrentDate();
    List<PromocodeRedemption> toDeactivate = Lists.newArrayList();
    activeRedemptions.forEach(r -> {
      if (r.getValidUntil() != null && r.getValidUntil().before(currentDate)) {
        r.setActive(false);
        toDeactivate.add(r);
      }
    });
    if (!toDeactivate.isEmpty()) {
      promocodeRedemptionDslRepository.saveMany(toDeactivate);
      activeRedemptions.removeAll(toDeactivate);
    }
    return activeRedemptions;
  }

  private List<PromocodeRedemption> getActiveRedemptions(Long riderId) {
    return promocodeRedemptionDslRepository.findActiveRedemptions(riderId);
  }

  private Long getRedemptionsCount(Rider rider) {
    return promocodeRedemptionDslRepository.countReferralRedemptions(rider);
  }

  private List<PromocodeRedemption> filterEligible(List<PromocodeRedemption> activeRedemptions, PromocodeUseRequest request) {
    return activeRedemptions.stream()
      .filter(r -> r.getPromocode().getCityBitmask() == null || cityCache.fromBitMask(r.getPromocode().getCityBitmask()).contains(request.getCityId()))
      .filter(r -> r.getPromocode().getCarTypeBitmask() == null || carTypesCache.fromBitMask(r.getPromocode().getCarTypeBitmask()).contains(request.getCarCategory()))
      .collect(Collectors.toList());
  }

  private void orderRedemptions(List<PromocodeRedemption> redemptions) {
    redemptions.sort((r1, r2) -> {
      if (r1.getValidUntil() == null) {
        return (r2.getValidUntil() == null) ? 0 : 1;
      }
      if (r2.getValidUntil() == null) {
        return -1;
      }
      return r1.getValidUntil().compareTo(r2.getValidUntil());
    });
  }
}
