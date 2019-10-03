package com.rideaustin.service.promocodes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.promocodes.PromocodeType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.PromocodeDslRepository;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.UnAuthorizedException;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.util.PromocodeBuilder;
import com.rideaustin.utils.RandomString;

@RunWith(MockitoJUnitRunner.class)
public class PromocodeServiceTest {

  private static final long PROMOCODE_ID = 123L;
  private static final double VAL10 = 10.00;
  private static final long VALUE10 = 10;

  private static final String START_DATE = "2016-01-01";
  private static final String END_DATE = "2016-01-31";
  private static final String MID_DATE = "2016-01-15";
  private static final String DATE_BEFORE = "2015-12-31";
  private static final String DATE_AFTER = "2016-02-01";
  private static final double VAL20 = 20.0;
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
  private static final String USER_USER_COM = "user@user.com";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private PromocodeDslRepository promocodeDslRepository;
  @Mock
  private PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;
  @Mock
  private TimeService timeService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private BeanFactory beanFactory;
  @Mock
  private PromocodeServiceConfig promocodeServiceConfig;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private CityCache cityCache;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private PublicPromocodeRedemptionPolicy publicPromocodeRedemptionPolicy;

  @InjectMocks
  private PromocodeService promocodeService;

  private String promocodeLiteral;
  private PromocodeBuilder defaultPromocode;
  private Rider rider;

  @Before
  public void setupTests() throws ParseException, RideAustinException {

    User user = new User();
    user.setEmail(USER_USER_COM);
    rider = new Rider();
    rider.setId(new Random().nextLong());
    rider.setUser(user);

    when(riderDslRepository.getRider(rider.getId())).thenReturn(rider);
    when(currentUserService.getUser()).thenReturn(rider.getUser());

    when(cityCache.fromBitMask(1)).thenReturn(Sets.newHashSet(1L));
    when(cityCache.fromBitMask(2)).thenReturn(Sets.newHashSet(2L));
    when(cityCache.fromBitMask(3)).thenReturn(Sets.newHashSet(1L, 2L));

    when(carTypesCache.fromBitMask(1)).thenReturn(Sets.newHashSet("REGULAR"));
    when(carTypesCache.fromBitMask(2)).thenReturn(Sets.newHashSet("SUV"));

    when(promocodeServiceConfig.getMaximumActiveRedeemedCredit()).thenReturn(BigDecimal.valueOf(500));
    when(promocodeServiceConfig.getUserPromocodeMaxRedemption()).thenReturn(20L);
    when(promocodeServiceConfig.getUserPromocodeFreeCreditAmount()).thenReturn(BigDecimal.valueOf(10));
    when(promocodeServiceConfig.getMaximumPromocodeValue()).thenReturn(BigDecimal.valueOf(100));

    when(promocodeServiceConfig.getUserPromoCodesValidityPeriod()).thenReturn(90);

    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(MID_DATE));

    promocodeLiteral = RandomString.generate();
    defaultPromocode = PromocodeBuilder.create(promocodeLiteral)
      .setValue(VAL10)
      .setParams(true)
      .setMaxRedemption(VALUE10)
      .setDatesRange(START_DATE, END_DATE)
      .setOwner(new Rider())
      .setCurrentRedemption(VALUE10 - 2);

    when(promocodeDslRepository.findByLiteral(promocodeLiteral)).thenReturn(defaultPromocode.get());
    when(beanFactory.getBean(any(Class.class))).thenReturn(publicPromocodeRedemptionPolicy);
    when(publicPromocodeRedemptionPolicy.useRedemption(any(), any()))
      .thenAnswer(invocationOnMock -> new PromocodeUseResult(true, BigDecimal.TEN, (PromocodeRedemption) invocationOnMock.getArguments()[1]));
    when(publicPromocodeRedemptionPolicy.dryRun(true)).thenCallRealMethod();
    when(publicPromocodeRedemptionPolicy.dryRun(false)).thenCallRealMethod();
  }

  @Test
  public void testGetPromocode() throws BadRequestException {
    promocodeService.getPromocode(PROMOCODE_ID);

    verify(promocodeDslRepository, times(1)).findOne(PROMOCODE_ID);
  }

  @Test
  public void testAddPromocodeAllRequired() throws RideAustinException {
    Promocode promocode = PromocodeBuilder.create(promocodeLiteral + "t2").setValue(VAL10)
      .setCities(3).setCarTypes(31).get();

    promocodeService.addPromocode(promocode);
    ArgumentCaptor<Promocode> promocodeCaptor = ArgumentCaptor.forClass(Promocode.class);
    verify(promocodeDslRepository).save(promocodeCaptor.capture());
    Promocode savedPromocode = promocodeCaptor.getValue();

    assertThat(savedPromocode.getCodeLiteral(), is(promocodeLiteral + "t2"));
    assertThat(savedPromocode.getCodeValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
  }

  @Test
  public void testUpdatePromocode() throws RideAustinException, ParseException {
    Promocode promocode = PromocodeBuilder.create().get();
    promocode.setCodeLiteral("asd");
    when(promocodeDslRepository.findOne(anyLong())).thenReturn(promocode);
    when(promocodeDslRepository.findByLiteral(eq(promocodeLiteral))).thenReturn(null);

    Promocode promocodeNew = PromocodeBuilder.create(promocodeLiteral + "ad").setValue(VAL10).setParams(false)
      .setMaxRedemption(VALUE10).setDatesRange("2016-01-01", "2016-01-31")
      .setCities(3).setCarTypes(31).get();
    promocodeService.updatePromocode(PROMOCODE_ID, promocodeNew);
    verify(promocodeDslRepository, times(1)).save(promocode);

    assertThat(promocode.getCodeLiteral(), is(promocodeLiteral + "ad"));
    assertThat(promocode.getCodeValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(promocode.getMaximumRedemption(), equalTo(VALUE10));
    assertThat(promocode.isNewRidersOnly(), is(false));
    assertThat(promocode.getStartsOn(), is(not(nullValue())));
    assertThat(promocode.getEndsOn(), is(not(nullValue())));
  }

  @Test
  public void updatePromocodeWithExistingLiteral() throws Exception {
    Promocode existingPromocode = new Promocode();
    existingPromocode.setCodeLiteral(promocodeLiteral);
    when(promocodeDslRepository.findOne(eq(PROMOCODE_ID))).thenReturn(existingPromocode);
    when(promocodeDslRepository.findByLiteral(eq(promocodeLiteral))).thenReturn(existingPromocode);

    Promocode promocodeNew = PromocodeBuilder.create(promocodeLiteral)
      .setValue(VAL10)
      .setParams(false)
      .setMaxRedemption(VALUE10)
      .setDatesRange("2016-01-01", "2016-01-31")
      .setCities(3).setCarTypes(31).get();
    promocodeService.updatePromocode(PROMOCODE_ID, promocodeNew);

    ArgumentCaptor<Promocode> captor = ArgumentCaptor.forClass(Promocode.class);
    verify(promocodeDslRepository, times(1)).save(captor.capture());
    assertEquals(VAL10, captor.getValue().getCodeValue().doubleValue(), 0.0);
    assertEquals(VALUE10, captor.getValue().getMaximumRedemption().longValue());
  }

  @Test
  public void updatePromocodeWithNewLiteral() throws Exception {
    String updatedCode = RandomString.generate();

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(String.format("Code literal %s is already in use", updatedCode));

    Promocode existingPromocode = new Promocode();
    existingPromocode.setCodeLiteral(promocodeLiteral);
    existingPromocode.setId(PROMOCODE_ID);
    when(promocodeDslRepository.findOne(eq(PROMOCODE_ID))).thenReturn(existingPromocode);
    Promocode existingLiteralPromocode = new Promocode();
    existingLiteralPromocode.setCodeLiteral(updatedCode);
    existingLiteralPromocode.setId(PROMOCODE_ID + 100);
    when(promocodeDslRepository.findByLiteral(eq(updatedCode))).thenReturn(existingLiteralPromocode);

    Promocode promocodeNew = PromocodeBuilder.create(updatedCode)
      .setValue(VAL10)
      .setParams(false)
      .setMaxRedemption(VALUE10).setDatesRange("2016-01-01", "2016-01-31")
      .setCities(3).setCarTypes(31).get();
    promocodeService.updatePromocode(PROMOCODE_ID, promocodeNew);
  }

  @Test
  public void testUsePromocodeNoRider() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Rider does not exist");
    when(riderDslRepository.getRider(anyLong())).thenReturn(null);
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeRiderUnauthenticated() throws Exception {
    expectedException.expect(UnAuthorizedException.class);
    expectedException.expectMessage("Rider not authorized");
    when(currentUserService.getUser()).thenReturn(null);
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void usePromocodeInvalidPromocode() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Sorry, invalid promocode");
    when(promocodeDslRepository.findByLiteral(anyString())).thenReturn(null);
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeMaxRedemptionExceeded() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Sorry, invalid promocode");
    when(promocodeDslRepository.findByLiteral(anyString()))
      .thenReturn(defaultPromocode.setCurrentRedemption(VALUE10).get());
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeStartDateToEarly() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Sorry, invalid promocode");
    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE_BEFORE));
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeStartDateToLate() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Sorry, invalid promocode");
    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE_AFTER));
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void usePromocodeCornerDates() throws Exception {
    defaultPromocode = defaultPromocode.setParams(false);
    when(promocodeDslRepository.findByLiteral(anyString())).thenReturn(defaultPromocode.get());

    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(START_DATE));
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(END_DATE));
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeAlreadyHasRides() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("This promocode is for new riders only");
    when(rideDslRepository.countCompletedRidesPerRider(anyObject()))
      .thenReturn(1L);
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void shouldNotUseOwnPromocode() throws Exception {
    // given
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("You cannot redeem your own promocode");
    when(promocodeDslRepository.findByLiteral(promocodeLiteral)).thenReturn(defaultPromocode.setOwner(rider).get());

    // when
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeAlreadyHasFreeCredit() throws Exception {
    User user = new User();
    Rider rider = new Rider();
    rider.setUser(user);

    when(riderDslRepository.getRider(anyLong())).thenReturn(rider);
    when(currentUserService.getUser()).thenReturn(user);
    when(promocodeRedemptionDslRepository.countReferralRedemptions(any())).thenReturn(1L);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("This promocode is for new riders only");

    promocodeService.applyPromocode(this.rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeAlreadyUsed() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("This promocode has already been used");
    when(promocodeRedemptionDslRepository.findPromocodeRedemption(anyObject(), anyObject()))
      .thenReturn(new PromocodeRedemption());
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
  }

  @Test
  public void testUsePromocodeFirstUse() throws Exception {
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
    ArgumentCaptor<BaseEntity> argument = ArgumentCaptor.forClass(BaseEntity.class);
    verify(promocodeDslRepository, times(1)).save(argument.capture());

    List<BaseEntity> arguments = argument.getAllValues();

    Promocode promocode = (Promocode) arguments.get(0);
    assertThat(promocode.getCurrentRedemption(), equalTo(VALUE10 - 1));
  }

  @Test
  public void testUsePromocodeSecondCodeUse() throws Exception {
    // given
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);
    when(promocodeDslRepository.findByLiteral(anyString())).thenReturn(defaultPromocode.setParams(false).
      setValue(VAL20).get());

    // when
    promocodeService.applyPromocode(rider.getId(), promocodeLiteral);

    // then
    ArgumentCaptor<Promocode> savedPromocodes = ArgumentCaptor.forClass(Promocode.class);
    verify(promocodeDslRepository, times(2)).save(savedPromocodes.capture());

    List<Promocode> arguments = savedPromocodes.getAllValues();

    Promocode promocode = arguments.get(1);
    assertThat(promocode.getCurrentRedemption(), equalTo(VALUE10));
  }

  @Test
  public void testAssignPromocode() throws Exception {
    promocodeService.assignRiderPromocode(rider);

    ArgumentCaptor<Promocode> savedPromocodes = ArgumentCaptor.forClass(Promocode.class);
    verify(promocodeDslRepository, times(1)).save(savedPromocodes.capture());

    Promocode promocode = savedPromocodes.getValue();
    assertThat(promocode.getOwner(), is(rider));
    assertThat(promocode.getPromocodeType(), is(PromocodeType.USER));
    assertThat(promocode.getCodeLiteral(), is(not(nullValue())));
    assertThat(promocode.getCodeValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(promocode.getMaximumRedemption(), equalTo(20L));

    assertThat(promocode.getMaximumUsesPerAccount(), equalTo(1));
    assertThat(promocode.getValidForNumberOfDays(), equalTo(90));

  }

  @Test
  public void testUsePromocodeNoRedemptions() {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    mockRedemptions(Lists.newArrayList());

    PromocodeUseResult result = promocodeService.usePromocode(request);

    verify(promocodeRedemptionDslRepository, never()).saveMany(any());
    assertThat(result.isSuccess(), is(false));
  }

  @Test
  public void testUsePromocodeSingleActiveRedemption() {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    mockRedemptions(Lists.newArrayList(
      mockActiveRedemption(20, 10, 1, null, 1, 1, false)));

    when(beanFactory.getBean(any(Class.class))).thenReturn(publicPromocodeRedemptionPolicy);
    when(publicPromocodeRedemptionPolicy.useRedemption(any(), any())).thenReturn(new PromocodeUseResult(true, BigDecimal.TEN, null));

    PromocodeUseResult result = promocodeService.usePromocode(request);

    verify(promocodeRedemptionDslRepository, never()).saveMany(any());
    assertThat(result.isSuccess(), is(true));
  }

  @Test
  public void testUsePromocodeSingleRedemptionNoLongerActive() throws Exception {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    List<PromocodeRedemption> redemptions =
      Lists.newArrayList(
        mockActiveRedemption(20, 10, 1, DATE_BEFORE, 1, 1, false));

    mockRedemptions(redemptions);

    PromocodeUseResult result = promocodeService.usePromocode(request);

    verify(promocodeRedemptionDslRepository, times(1)).saveMany(any());
    assertThat(result.isSuccess(), is(false));
  }

  @Test
  public void testUsePromocodeTwoRedemptionsOneActive() throws Exception {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    mockRedemptions(Lists.newArrayList(
      mockActiveRedemption(20, 10, 1, DATE_BEFORE, 1, 1, false),
      mockActiveRedemption(20, 10, 1, DATE_AFTER, 1, 1, false)));

    PromocodeUseResult result = promocodeService.usePromocode(request);

    verify(promocodeRedemptionDslRepository, times(1)).saveMany(any());
    assertThat(result.isSuccess(), is(true));
  }

  @Test
  public void testUsePromocodeNotEligibleDifferentCity() throws Exception {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    mockRedemptions(Lists.newArrayList(
      mockActiveRedemption(20, 10, 1, null, 2, 1, false)));

    PromocodeUseResult result = promocodeService.usePromocode(request);

    assertThat(result.isSuccess(), is(false));
  }

  @Test
  public void testUsePromocodeNotEligibleDifferentCarType() throws Exception {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    mockRedemptions(Lists.newArrayList(
      mockActiveRedemption(20, 10, 1, null, 1, 2, false)));

    PromocodeUseResult result = promocodeService.usePromocode(request);

    assertThat(result.isSuccess(), is(false));
  }

  @Test
  public void testUsePromocodeShouldUseNextRideOnlyFirst() throws Exception {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    List<PromocodeRedemption> redemptions =
      Lists.newArrayList(
        mockActiveRedemption(20, 10, 1, null, 1, 1, false),
        mockActiveRedemption(20, 10, 1, null, 1, 1, true));

    mockRedemptions(redemptions);

    when(publicPromocodeRedemptionPolicy.useRedemption(any(), any()))
      .thenAnswer(invocationOnMock -> {
        PromocodeRedemption pr = (PromocodeRedemption) invocationOnMock.getArguments()[1];
        pr.setActive(false);
        return new PromocodeUseResult(true, BigDecimal.TEN, pr);
      });

    PromocodeUseResult result = promocodeService.usePromocode(request);

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getAffectedPromocodeRedemption(), is(redemptions.get(1)));
    assertThat(result.getAffectedPromocodeRedemption().isActive(), is(false));
  }

  @Test
  public void testUsePromocodeStartedRedemptionShouldGoFirst() throws Exception {
    PromocodeUseRequest request = mockRequest(20.0, "REGULAR", 1L);
    PromocodeRedemption redemption1 = mockActiveRedemption(20, 20, 1, END_DATE, 1, 1, false);
    PromocodeRedemption redemption2 = mockActiveRedemption(20, 10, 1, DATE_AFTER, 1, 1, false);
    List<PromocodeRedemption> redemptions = Lists.newArrayList(redemption1, redemption2);

    mockRedemptions(redemptions);
    PromocodeUseResult result = promocodeService.usePromocode(request);

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getAffectedPromocodeRedemption(), is(redemption2));
  }

  private PromocodeRedemption mockActiveRedemption(double initialValue, double remainingValue,
    int usedTimes, String validUntil, Integer cityBitmask, Integer carTypesBitmask, boolean nextTripOnly) {
    PromocodeRedemption redemption = new PromocodeRedemption();
    redemption.setRider(rider);
    redemption.setActive(true);
    redemption.setId((new Random()).nextLong());
    redemption.setRemainingValue(BigDecimal.valueOf(remainingValue));
    redemption.setOriginalValue(BigDecimal.valueOf(initialValue));
    redemption.setNumberOfTimesUsed(usedTimes);
    redemption.setPromocode(mockPromocode(cityBitmask, carTypesBitmask, nextTripOnly));
    if (StringUtils.isNoneEmpty(validUntil)) {
      try {
        redemption.setValidUntil(DATE_FORMATTER.parse(validUntil));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return redemption;
  }

  private Promocode mockPromocode(Integer cityBitmask, Integer carTypeBitmask, boolean nextTripOnly) {
    Promocode p = new Promocode();
    p.setCityBitmask(cityBitmask);
    p.setCarTypeBitmask(carTypeBitmask);
    p.setNextTripOnly(nextTripOnly);
    return p;
  }

  private void mockRedemptions(List<PromocodeRedemption> redemptions) {
    when(promocodeRedemptionDslRepository.findActiveRedemptions(any())).thenReturn(redemptions);
  }

  private PromocodeUseRequest mockRequest(double amount, String carCategory, Long cityId) {
    return PromocodeUseRequest.builder()
      .riderId(rider.getId())
      .fareCreditAmount(BigDecimal.valueOf(amount))
      .cityId(cityId)
      .carCategory(carCategory)
      .valid(true)
      .build();
  }

}