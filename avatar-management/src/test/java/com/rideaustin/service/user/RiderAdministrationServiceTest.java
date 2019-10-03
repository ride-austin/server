package com.rideaustin.service.user;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListRidersParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.BaseAvatarService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SchedulerService;

public class RiderAdministrationServiceTest {

  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private SchedulerService schedulerService;
  @Mock
  private ObjectMapper mapper;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private BaseAvatarService baseAvatarService;

  private RiderAdministrationService testedInstance;

  private Rider rider;
  private Page<Rider> ridersPage;

  @Before
  public void setUp() throws Exception {
    rider = new Rider();
    ridersPage = new PageImpl<>(Collections.singletonList(rider));

    MockitoAnnotations.initMocks(this);
    testedInstance = new RiderAdministrationService(riderDslRepository, schedulerService,
      mapper, currentUserService, baseAvatarService);

    when(riderDslRepository.save(any(Rider.class))).thenAnswer(call -> call.getArguments()[0]);
    when(riderDslRepository.findRiders(any(), any())).thenReturn(ridersPage);
  }

  @Test
  public void listRidersPage() {
    // when
    Page<Rider> riders = testedInstance.listRiders(new ListRidersParams(), new PagingParams());

    // then
    assertEquals(ridersPage, riders);
  }

  @Test
  public void testListRidersEnrichLastLoginDate() throws RideAustinException {
    // when
    testedInstance.listRiders(new ListRidersParams(), new PagingParams());

    // then
    verify(baseAvatarService, times(1)).enrichAvatarWithLastLoginDate(ridersPage);
  }
}