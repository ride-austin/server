package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.repo.dsl.AreaQueueEntryDslRepository;
import com.rideaustin.service.areaqueue.AreaQueueEntryService;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;

@RunWith(MockitoJUnitRunner.class)
public class AreaQueueEntryServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  private AreaQueueEntryDslRepository areaQueueEntryDslRepository;
  @Mock
  private ObjectLocationService<OnlineDriverDto> objectLocationService;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;

  private AreaQueueEntryService areaQueueEntryService;

  private List<Area> areas = new ArrayList<>();
  private Area country = new Area();
  private List<AreaQueueEntry> entries = new ArrayList<>();
  private AreaQueueEntry entry = new AreaQueueEntry();

  private Area a = new Area();

  @Before
  public void setup() throws Exception {

    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("14.3200360,54.6181640 14.3200360,48.9513665 23.8856970,48.9513665 23.8856970,54.6181640");
    country.setAreaGeometry(areaGeometry);
    country.setId(1L);

    AreaGeometry areaGeometry1 = new AreaGeometry();
    areaGeometry1.setCsvGeometry("1,1 1,10 10,10 10,1");
    a.setAreaGeometry(areaGeometry1);
    a.setId(2L);
    areas.add(a);
    areas.add(country);

    ActiveDriver ad = new ActiveDriver();
    ad.setStatus(ActiveDriverStatus.AVAILABLE);
    LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(52.225678);
    locationObject.setLongitude(20.981819);
    ad.setLocationObject(locationObject);
    entry.setActiveDriver(ad);
    entry.setArea(a);
    entries.add(entry);

    areaQueueEntryService = new AreaQueueEntryService(areaQueueEntryDslRepository, objectLocationService, requestedDriversRegistry);

  }

  @Test
  public void testGetEntries() {
    when(areaQueueEntryDslRepository.findByArea(country)).thenReturn(entries);
    List<AreaQueueEntry> re = areaQueueEntryService.getEntries(country);

    verify(areaQueueEntryDslRepository, times(1)).findByArea(country);
    assertThat(re, is(entries));

  }

  @Test
  public void testGetCurrentActiveDriverAreaQueueEntr() {
    when(areaQueueEntryDslRepository.findEnabledByActiveDriver(any())).thenReturn(entries);
    final ActiveDriver activeDriver = new ActiveDriver();
    List<AreaQueueEntry> re = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(activeDriver.getId());
    verify(areaQueueEntryDslRepository, times(1)).findEnabledByActiveDriver(any());
    assertThat(re.get(0), is(entries.get(0)));
  }

  @Test
  public void testGetCurrentActiveDriverAreaQueueEntryNull() {
    when(areaQueueEntryDslRepository.findEnabledByActiveDriver(any())).thenReturn(null);
    final ActiveDriver activeDriver = new ActiveDriver();
    List<AreaQueueEntry> re = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(activeDriver.getId());
    verify(areaQueueEntryDslRepository, times(1)).findEnabledByActiveDriver(any());
    assertThat(re, is(nullValue()));
  }

}
