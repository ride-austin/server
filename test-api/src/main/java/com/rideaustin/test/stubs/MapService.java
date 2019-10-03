package com.rideaustin.test.stubs;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.service.thirdparty.GeoApiClientFactory;
import com.rideaustin.service.thirdparty.RestTemplateFactory;
import com.rideaustin.utils.map.LocationCorrector;

@Service
@Profile("itest")
public class MapService extends com.rideaustin.service.MapService {

  public MapService(Environment env, GeoApiClientFactory geoApiClientFactory, RestTemplateFactory restTemplateFactory, LocationCorrector locationCorrector) {
    super(env, geoApiClientFactory, restTemplateFactory, locationCorrector);
  }

  @Nonnull
  @Override
  public byte[] generateMap(@Nonnull List<LatLng> points) {
    return new byte[0];
  }

  @Nonnull
  @Override
  public byte[] generateMapMinimized(@Nonnull List<LatLng> points) {
    return new byte[0];
  }
}
