package com.rideaustin.service.promocodes;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.util.PromocodeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class PublicPromocodeRedemptionPolicyTest {

  private static final double VAL5 = 5.0;
  private static final double VAL10 = 10.0;
  private static final String DATE = "2016-01-01";
  private static final String DATE_TO = "2016-03-31";
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

  @Mock
  private TimeService timeService;
  @Mock
  private PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;



  @InjectMocks
  private PublicPromocodeRedemptionPolicy publicPromocodeFreeCreditPolicy;

  @Test
  public void testPublicPromocodeApplyPromocodeNoValidityDate() throws Exception {
    Rider rider = mockRider();
    Promocode promocode = PromocodeBuilder.create().setValue(VAL10).get();

    publicPromocodeFreeCreditPolicy.applyPromocode(rider, promocode);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(redemption.getValidUntil(), is(nullValue()));
    assertThat(redemption.getRider(), is(rider));
    assertThat(redemption.getPromocode(), is(promocode));
    assertThat(redemption.isActive(), is(true));
    assertThat(redemption.getOriginalValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeApplyPromocodeWithValidityDate() throws Exception {
    Rider rider = mockRider();
    Promocode promocode = PromocodeBuilder.create().setValue(VAL10).setValidityParams(DATE, null).get();

    publicPromocodeFreeCreditPolicy.applyPromocode(rider, promocode);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(redemption.getValidUntil(), is(DATE_FORMATTER.parse(DATE)));
    assertThat(redemption.getRider(), is(rider));
    assertThat(redemption.getPromocode(), is(promocode));
    assertThat(redemption.isActive(), is(true));
    assertThat(redemption.getOriginalValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeApplyPromocodeWithValidityPeriod() throws Exception {
    Rider rider = mockRider();
    Promocode promocode = PromocodeBuilder.create().setValue(VAL10).setValidityParams(null, 90).get();

    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE));

    publicPromocodeFreeCreditPolicy.applyPromocode(rider, promocode);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(redemption.getValidUntil(), is(DATE_FORMATTER.parse(DATE_TO)));
    assertThat(redemption.getRider(), is(rider));
    assertThat(redemption.getPromocode(), is(promocode));
    assertThat(redemption.isActive(), is(true));
    assertThat(redemption.getOriginalValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeUserRedemptionEqualCredit() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL5);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL5, 1, 2);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.getNumberOfTimesUsed(), is(2));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeUserRedemptionLessCredit() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL10);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL5, 1, 2);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.getNumberOfTimesUsed(), is(2));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeUserRedemptionMoreCredit() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL5);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL10, 1, 2);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.getNumberOfTimesUsed(), is(2));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeUserRedemptionDeactivateNumberOfUses() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL5);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL10, 1, 2);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.isActive(), is(false));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeUserRedemptionDeactivateNoCredit() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL10);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL10, 1, 3);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.isActive(), is(false));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testPublicPromocodeUserRedemptionDeactivateNextTripOnly() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL5);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL10, 1, 3);
    promocodeRedemption.getPromocode().setNextTripOnly(true);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.isActive(), is(false));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.valueOf(VAL5), BigDecimal.ZERO)));
  }


  @Test
  public void testPublicPromocodeUserRedemptionMaxUsesPerAccountIsNull() throws PromocodeException {
    PromocodeUseRequest request = mockRequest(VAL10);
    PromocodeRedemption promocodeRedemption = mockRedemption(VAL10, 1, null);

    PromocodeUseResult result = publicPromocodeFreeCreditPolicy.useRedemption(request, promocodeRedemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository).save(argument.capture());

    PromocodeRedemption redemption = argument.getValue();
    assertThat(result.getPromocodeCreditUsed(), is(closeTo(BigDecimal.TEN, BigDecimal.ZERO)));
    assertThat(result.isSuccess(), is(Boolean.TRUE));
    assertThat(redemption.isActive(), is(false));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
    assertThat(redemption.getRemainingValue(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  static PromocodeRedemption mockRedemption(double remaining, int used, Integer max) {
    Promocode promocode = new Promocode();
    promocode.setMaximumUsesPerAccount(max);
    PromocodeRedemption redemption = new PromocodeRedemption();
    redemption.setPromocode(promocode);
    redemption.setRemainingValue(BigDecimal.valueOf(remaining));
    redemption.setNumberOfTimesUsed(used);
    return redemption;
  }

  static PromocodeUseRequest mockRequest(double requestedAmount) {
    return PromocodeUseRequest.builder()
      .fareCreditAmount(BigDecimal.valueOf(requestedAmount))
      .build();
  }

  static PromocodeUseRequest mockRequest(double requestedAmount, Rider rider) {
    return PromocodeUseRequest.builder()
      .fareCreditAmount(BigDecimal.valueOf(requestedAmount))
      .riderId(rider.getId())
      .build();
  }

  static Rider mockRider() {
    return new Rider();
  }

}