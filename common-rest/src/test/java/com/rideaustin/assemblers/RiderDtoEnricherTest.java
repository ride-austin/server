package com.rideaustin.assemblers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.user.BlockedDeviceService;

public class RiderDtoEnricherTest {

  @Mock
  private BlockedDeviceService blockedDeviceService;

  private RiderDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RiderDtoEnricher(blockedDeviceService);
  }

  @Test
  public void enrichSkipsNull() {
    final RiderDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichFillsDeviceBlockedFlag() {
    final boolean blocked = true;
    when(blockedDeviceService.isInBlocklist(anyLong())).thenReturn(blocked);

    final RiderDto result = testedInstance.enrich(new RiderDto(1L));

    assertEquals(blocked, result.getUser().isDeviceBlocked());
  }
}