package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.service.user.DriverTypeCache;

@RunWith(MockitoJUnitRunner.class)
public class DriverTypeServiceTest {

  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private CarTypesCache carTypesCache;

  private DriverTypeService driverTypeService;

  @Before
  public void setup() throws Exception {
    driverTypeService = new DriverTypeService(driverTypeCache, carTypesCache);
    CarTypesUtils.setCarTypesCache(carTypesCache);
  }

  @Test
  public void getGetAll() throws RideAustinException {
    Map<String, DriverType> rw = new HashMap<>();
    rw.put("WOMEN_ONLY", prepareDriverType(6, "WOMEN ONLY", "WOMEN_ONLY"));
    when(driverTypeCache.getDriverTypes()).thenReturn(rw);
    Map<String, DriverType> ares = driverTypeService.getAll();
    assertThat(ares.size(), is(1));
    assertThat(ares.get("WOMEN_ONLY").getName(), is("WOMEN_ONLY"));
  }

  @Test
  public void testGetOne() throws RideAustinException {
    DriverType dt = prepareDriverType(6, "WOMEN ONLY", "WOMEN_ONLY");
    when(driverTypeCache.getDriverType(any())).thenReturn(dt);
    DriverType loaded = driverTypeService.getOne("AAA");
    assertThat(loaded.getName(), is(dt.getName()));
  }

  @Test
  public void testCheckIfDriverTypesSupportCarCategories() throws RideAustinException {

    Set<String> carCategories = Collections.singleton("PREMIUM");
    when(carTypesCache.fromBitMask(anyInt())).thenReturn(carCategories);
    DriverType dt = prepareDriverType(6, "WOMEN ONLY", "WOMEN_ONLY");
    CityDriverType cdt = new CityDriverType();
    dt.setCityDriverTypes(new HashSet<>());
    cdt.setCityId(1L);
    dt.getCityDriverTypes().add(cdt);
    when(driverTypeCache.getDriverType(any())).thenReturn(dt);

    boolean support = driverTypeService
      .checkIfDriverTypesSupportCarCategories(carCategories, Collections.singleton("WOMEN_ONLY"), 1L);
    assertThat(support, is(true));
  }

  @Test
  public void testCheckIfDriverTypesSupportCarCategoriesWrong() throws RideAustinException {

    Set<String> carCategories1 = Collections.singleton("PREMIUM");
    Set<String> carCategories6 = Collections.singleton("SUV");
    when(carTypesCache.fromBitMask(6)).thenReturn(carCategories6);
    when(carTypesCache.fromBitMask(1)).thenReturn(carCategories1);
    DriverType dt = prepareDriverType(6, "WOMEN ONLY", "WOMEN_ONLY");
    CityDriverType cdt = new CityDriverType();
    dt.setCityDriverTypes(new HashSet<>());
    cdt.setCityId(1L);
    dt.getCityDriverTypes().add(cdt);
    when(driverTypeCache.getDriverType(any())).thenReturn(dt);

    boolean support = driverTypeService
      .checkIfDriverTypesSupportCarCategories(carCategories6, Collections.singleton("WOMEN_ONLY"), 1L);
    assertThat(support, is(false));
  }

  public DriverType prepareDriverType(int bitmask, String description, String name) {

    CityDriverType cdt = new CityDriverType();
    cdt.setAvailableInCategories(CarTypesUtils.fromBitMask(bitmask));
    cdt.setCityId(1L);
    cdt.setEnabled(true);
    DriverType dt = new DriverType();
    dt.setBitmask(bitmask);
    dt.setCityDriverTypes(new HashSet<>());
    dt.getCityDriverTypes().add(cdt);
    dt.setCreatedDate(new Date());
    dt.setDescription(description);
    dt.setEnabled(true);
    dt.setName(name);
    dt.setCreatedDate(new Date());
    cdt.setDriverType(dt);
    return dt;
  }

  @Test
  public void testisCitySupportDriverType() {
    DriverType dt = prepareDriverType(1, "aa", "WOMEN_ONLY");

    when(driverTypeCache.getDriverType("WOMEN_ONLY")).thenReturn(dt);

    boolean isSupported1 = driverTypeService.isCitySupportDriverType("WOMEN_ONLY", 1L);
    boolean isSupported2 = driverTypeService.isCitySupportDriverType("WOMEN_ONLY", 2L);
    boolean isSupported3 = driverTypeService.isCitySupportDriverType("WOMEN_ONLY_2", 1L);
    boolean isSupported4 = driverTypeService.isCitySupportDriverType("WOMAN_ONLY", 1L);

    assertThat(isSupported1, is(true));
    assertThat(isSupported2, is(false));
    assertThat(isSupported3, is(false));
    assertThat(isSupported4, is(false));

  }
}
