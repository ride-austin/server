package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;

@WebAppConfiguration
@ActiveProfiles({"dev","itest"})
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:resources/dev.properties")
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class}, initializers = RAApplicationInitializer.class)
public class GeoApiClientGoogleImplIT {

  @Inject
  private GeoApiClientGoogleImpl clientGoogle;

  @Test
  public void shouldGetGoogleMiddlePoints() {
    // given
    LatLng from = new LatLng(30.202596, -97.667001);
    LatLng to = new LatLng(30.213537, -97.658422);

    // when
    List<LatLng> result = clientGoogle.getGoogleMiddlePoints(from, to);

    // then
    assertNotNull(result);
    assertTrue("There are points between", result.size() > 0);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForIntoOceanTrip() {
    // given
    LatLng from = new LatLng(30.202596, -97.667001);
    LatLng to = new LatLng(26.127089, -92.747618);

    // when
    List<LatLng> result = clientGoogle.getGoogleMiddlePoints(from, to);

    // then
    assertTrue("There are no points between", CollectionUtils.isEmpty(result));
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForSamePlaceTravel() {
    // given
    LatLng from = new LatLng(30.202596, -97.667001);
    LatLng to = new LatLng(30.202596, -97.667001);

    // when
    List<LatLng> result = clientGoogle.getGoogleMiddlePoints(from, to);

    // then
    assertTrue("There are no points between", CollectionUtils.isEmpty(result));
  }
}