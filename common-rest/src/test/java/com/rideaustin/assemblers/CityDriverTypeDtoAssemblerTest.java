package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.rest.model.CityDriverTypeDto;

public class CityDriverTypeDtoAssemblerTest {

  private CityDriverTypeDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new CityDriverTypeDtoAssembler(new ObjectMapper());
  }

  @Test
  public void toDtoSkipsNull() {
    final CityDriverTypeDto result = testedInstance.toDto((CityDriverType) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    final Long cityId = 1L;
    final DriverType driverType = new DriverType();
    final String description = "Regular rides";
    final String name = "Standard";
    final Set<String> categories = Collections.singleton("REGULAR");

    driverType.setDescription(description);
    driverType.setName(name);
    final CityDriverType cityDriverType = new CityDriverType();
    cityDriverType.setConfigurationClass(CityDriverType.DefaultDriverTypeConfiguration.class);
    cityDriverType.setConfiguration("{\"searchHandlerClass\":\"com.rideaustin.service.DefaultSearchDriverHandler\"}");
    cityDriverType.setAvailableInCategories(categories);
    cityDriverType.setCityId(cityId);
    cityDriverType.setDriverType(driverType);

    final CityDriverTypeDto result = testedInstance.toDto(cityDriverType);

    assertEquals(cityId, result.getCityId());
    assertEquals(description, result.getDescription());
    assertEquals(name, result.getName());
    assertEquals(categories, result.getAvailableInCategories());
    assertEquals("com.rideaustin.service.DefaultSearchDriverHandler", result.getConfiguration().getSearchHandlerClass());
  }

}