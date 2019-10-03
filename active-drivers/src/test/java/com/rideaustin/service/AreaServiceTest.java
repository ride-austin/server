package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.repo.dsl.AreaDslRepository;
import com.rideaustin.service.areaqueue.AreaCache;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.utils.GeometryUtils;

@RunWith(MockitoJUnitRunner.class)
public class AreaServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private AreaDslRepository areaDslRepository;
  @Mock
  private AreaCache areaCache;

  private AreaService areaService;

  private List<Area> areas = new ArrayList<>();
  private Area country = new Area();

  @Before
  public void setup() throws Exception {

    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("14.3200360,54.6181640 14.3200360,48.9513665 23.8856970,48.9513665 23.8856970,54.6181640");
    country.setAreaGeometry(areaGeometry);


    Area a = new Area();
    AreaGeometry areaGeometry1 = new AreaGeometry();
    areaGeometry1.setCsvGeometry("1,1 1,10 10,10 10,1");
    a.setAreaGeometry(areaGeometry1);
    areas.add(a);
    areas.add(country);

    when(areaCache.getAllAreas()).thenReturn(areas);
    when(areaCache.getAreasPerCity(1L)).thenReturn(areas);
    when(areaDslRepository.findOne(any())).thenReturn(a);
    areaService = new AreaService(areaCache, areaDslRepository);

  }

  @Test
  public void testGetAreas() {
    Collection<Area> ares = areaService.getAllAreas();
    assertThat(ares.size(), is(2));
    verify(areaCache, times(1)).getAllAreas();
  }

  @Test
  public void testGetByIdMultipleTimes() {
    areaService.getById(1L);
    areaService.getById(1L);
    verify(areaDslRepository, times(0)).findAll();
    verify(areaDslRepository, times(2)).findOne(any());
  }

  @Test
  public void testGetIsInsideAreaInCountry() {
    boolean isInside1 = GeometryUtils.isInsideArea(country, new LatLng(52.225678, 20.981819));
    assertThat(isInside1, is(true));
  }

  @Test
  public void testGetIsOutsideAreaInCountry() {
    boolean isInside1 = GeometryUtils.isInsideArea(country, new LatLng(50.445366, 30.530002));
    assertThat(isInside1, is(false));
  }

  @Test
  public void getAreaByLocation() {
    Area area = areaService.isInArea(mockActiveDriver(52.225678, 20.981819), 1L);
    assertThat(area, is(country));
  }

  @Test
  public void testGetAreaByLocationOutside() {
    Area area = areaService.isInArea(mockActiveDriver(50.445366, 30.530002), 1L);
    assertThat(area, is(nullValue()));
  }

  private OnlineDriverDto mockActiveDriver(double lat, double lng) {
    OnlineDriverDto ad = new OnlineDriverDto();
    LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(lat);
    locationObject.setLongitude(lng);
    ad.setLocationObject(locationObject);
    return ad;
  }

}
