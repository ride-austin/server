package com.rideaustin.service;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.rideaustin.model.City;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.service.user.BlockedDeviceService;
import com.rideaustin.service.user.RiderService;

@RunWith(MockitoJUnitRunner.class)
public class RiderServiceTest {

  @InjectMocks
  private RiderService riderService;

  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private UserService userService;
  @Mock
  private EmailService emailService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private StripeService stripeService;
  @Mock
  private PromocodeService promocodeService;
  @Mock
  private CityService cityService;
  @Mock
  private BaseAvatarService baseAvatarService;
  @Mock
  private SessionService sessionService;
  @Mock
  private BlockedDeviceService blockedDeviceService;

  private Rider rider;
  private Page<Rider> ridersPage;
  private User user;

  @Before
  public void setup() throws Exception {
    rider = new Rider();
    ridersPage = new PageImpl<>(Collections.singletonList(rider));
    user = new User();
    fillUser(user);
    rider.setUser(user);
    when(userDslRepository.save(any(User.class))).thenAnswer(call -> call.getArguments()[0]);
    when(riderDslRepository.save(any(Rider.class))).thenAnswer(call -> call.getArguments()[0]);
    when(riderDslRepository.findRiders(any(), any())).thenReturn(ridersPage);
    when(cityService.getCityForCurrentClientAppVersionContext()).thenReturn(createCity());
  }

  @Test
  public void testFindRiderEnrichLastLoginDate() throws RideAustinException {
    // given
    long riderId = 1L;
    when(riderDslRepository.getRiderWithDependencies(eq(riderId))).thenReturn(rider);
    when(currentUserService.getUser()).thenReturn(user);

    // when
    riderService.findRider(riderId);

    // then
    verify(baseAvatarService, times(1)).enrichAvatarWithLastLoginDate(rider);
  }

  private City createCity() {
    City city = new City();
    city.setAppName("RideAustin");
    city.setContactEmail("contact@ridesomhere.com");
    return city;
  }

  private void fillUser(User user) {
    user.setEmail("test.create.rider@example.com");
    user.setFirstname("Test");
    user.setLastname("Rider");
    user.setRawPassword("123");
    user.setPhoneNumber("9876543210");
  }
}