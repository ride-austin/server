package com.rideaustin.service.promocodes;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
public class UserPromocodeRedemptionPolicyTest {

  private static final double VAL10 = 10.0;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private TimeService timeService;
  @Mock
  private PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;

  @InjectMocks
  private UserPromocodeRedemptionPolicy userPromocodeFreeCreditPolicy;

  @Test
  public void testApplyPromocodeSameRider() throws PromocodeException {
    expectedException.expect(PromocodeException.class);
    expectedException.expectMessage("Invalid code");

    Rider rider = new Rider();
    rider.setId(1L);

    Promocode promocode = PromocodeBuilder.create().setValue(VAL10).setOwner(rider).get();
    userPromocodeFreeCreditPolicy.applyPromocode(rider, promocode);
  }

  @Test
  public void testUseRedemptionOwner() throws PromocodeException {
    Rider rider = PublicPromocodeRedemptionPolicyTest.mockRider();
    PromocodeUseRequest request = PublicPromocodeRedemptionPolicyTest.mockRequest(10, rider);
    PromocodeRedemption redemption = PublicPromocodeRedemptionPolicyTest.mockRedemption(10, 1, 2);
    redemption.setRider(rider);
    redemption.getPromocode().setOwner(rider);

    PromocodeUseResult result = userPromocodeFreeCreditPolicy.useRedemption(request, redemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository, times(1)).save(argument.capture());

    PromocodeRedemption resultingRedemptions = argument.getValue();

    assertThat(resultingRedemptions.isAppliedToOwner(), is(false));
  }

  @Test
  public void testUseRedemptionNotOwnerNotApplied() throws PromocodeException {
    Rider rider = PublicPromocodeRedemptionPolicyTest.mockRider();
    rider.setId(2);
    PromocodeUseRequest request = PublicPromocodeRedemptionPolicyTest.mockRequest(10, new Rider());
    PromocodeRedemption redemption = PublicPromocodeRedemptionPolicyTest.mockRedemption(10, 1, 2);
    redemption.getPromocode().setOwner(rider);

    PromocodeUseResult result = userPromocodeFreeCreditPolicy.useRedemption(request, redemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository, times(2)).save(argument.capture());

    PromocodeRedemption resultingRedemptions = argument.getValue();

    assertThat(resultingRedemptions.isAppliedToOwner(), is(true));
  }

  @Test
  public void testUseRedemptionNotOwnerApplied() throws PromocodeException {
    Rider rider = PublicPromocodeRedemptionPolicyTest.mockRider();
    rider.setId(2);
    PromocodeUseRequest request = PublicPromocodeRedemptionPolicyTest.mockRequest(10, new Rider());
    PromocodeRedemption redemption = PublicPromocodeRedemptionPolicyTest.mockRedemption(10, 1, 2);
    redemption.getPromocode().setOwner(rider);
    redemption.setAppliedToOwner(true);

    PromocodeUseResult result = userPromocodeFreeCreditPolicy.useRedemption(request, redemption);

    ArgumentCaptor<PromocodeRedemption> argument = ArgumentCaptor.forClass(PromocodeRedemption.class);
    verify(promocodeRedemptionDslRepository, times(1)).save(argument.capture());

    PromocodeRedemption resultingRedemptions = argument.getValue();

    assertThat(resultingRedemptions.isAppliedToOwner(), is(true));
  }

}