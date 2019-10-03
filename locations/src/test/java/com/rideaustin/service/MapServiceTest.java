package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Duration;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.rideaustin.model.Address;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.thirdparty.GeoApiClient;
import com.rideaustin.service.thirdparty.GeoApiClientFactory;
import com.rideaustin.service.thirdparty.RestTemplateFactory;
import com.rideaustin.utils.map.LocationCorrector;

@RunWith(MockitoJUnitRunner.class)
public class MapServiceTest {

  @Mock
  private Environment env;
  @Mock
  private GeoApiClientFactory geoApiClientFactory;
  @Mock
  private GeoApiClient geoApiClient;
  @Mock
  private RestTemplateFactory restTemplateFactory;
  @Mock
  private LocationCorrector locationCorrector;
  private MapService mapService;

  private Address address = new Address();

  @Before
  public void setup() {
    when(env.getProperty("google.roads.api.enabled", Boolean.class, true)).thenReturn(true);
    when(env.getProperty("dispatch.addSecondsToDrivingTime", Long.class, 0L)).thenReturn(120L);
    when(env.getProperty("driver.validate.zipcode", Boolean.class, false)).thenReturn(false);

    mapService = new MapService(env, geoApiClientFactory, restTemplateFactory, locationCorrector);


    DistanceMatrixRow row = new DistanceMatrixRow();
    DistanceMatrixElement element = new DistanceMatrixElement();
    element.status = DistanceMatrixElementStatus.OK;
    element.distance = new Distance();
    element.distance.inMeters = 1L;
    element.duration = new Duration();
    element.duration.inSeconds = 1L;
    row.elements = new DistanceMatrixElement[]{element};
    DistanceMatrix matrix = new DistanceMatrix(new String[0], new String[0], new DistanceMatrixRow[]{row});
    when(geoApiClientFactory.createGeoApiClient()).thenReturn(geoApiClient);
    when(geoApiClient.getDrivingDistance(any(LatLng.class), any(LatLng[].class))).thenReturn(matrix);
  }

  @Test(expected = BadRequestException.class)
  public void testNormalizeNonExistingAddress() throws RideAustinException {
    mapService.normalizeAddress(address);
  }

  @Test(expected = BadRequestException.class)
  public void testNormalizeAddressMultipleResults() throws Exception {
    when(geoApiClient.geocodeAddress(address)).thenReturn(new GeocodingResult[2]);
    mapService.normalizeAddress(address);
  }

  @Test
  public void testNormalizeAddress() throws Exception {
    address.setAddress("Austin 9625 Rainlilly Ln");
    Address address = new Address();
    address.setAddress("9625 Rainlilly Ln, Austin, TX 78759, USA");
    address.setZipCode("78759");

    GeocodingResult geocodingResult = new GeocodingResult();
    geocodingResult.formattedAddress = address.getAddress();
    geocodingResult.addressComponents = new AddressComponent[]{
      createComponent(AddressComponentType.POSTAL_CODE, address.getZipCode())
    };

    when(geoApiClient.geocodeAddress(this.address)).thenReturn(new GeocodingResult[]{geocodingResult});

    assertThat(mapService.normalizeAddress(this.address), is(address));
  }

  private AddressComponent createComponent(AddressComponentType type, String value) {
    AddressComponent zipComponent = new AddressComponent();
    zipComponent.types = new AddressComponentType[]{type};
    zipComponent.shortName = value;
    return zipComponent;
  }

  @Test
  public void testTimeToToAirportFromParkingWithWorkaround() {
    OnlineDriverDto ad = new OnlineDriverDto();
    LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(30.212835);
    locationObject.setLongitude(-97.665632);
    ad.setLocationObject(locationObject);
    mapService.updateTimeToDrive(Collections.singletonList(ad), 30.197245, -97.666457, true);
  }

  @Test
  public void testTimeToAirportFromParkingWithoutWorkaround() {
    List<OnlineDriverDto> adl = new ArrayList<>();
    OnlineDriverDto ad = new OnlineDriverDto();
    LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(30.212835);
    locationObject.setLongitude(-97.665632);
    ad.setLocationObject(locationObject);
    adl.add(ad);
    mapService.updateTimeToDrive(adl, 30.197245, -97.666457, true);
  }

  @Test
  public void testZipCodeLoading() throws Exception {
    double lat = 30.197245;
    double lng = -97.666457;

    String address = "2500 Hwy 71 E, Austin";
    String zipCode = "78617";

    GeocodingResult geocodingResult = new GeocodingResult();
    geocodingResult.addressComponents = new AddressComponent[]{
      createComponent(AddressComponentType.STREET_NUMBER, "2500"),
      createComponent(AddressComponentType.ROUTE, "Hwy 71 E"),
      createComponent(AddressComponentType.LOCALITY, "Austin"),
      createComponent(AddressComponentType.POSTAL_CODE, zipCode),
    };

    when(geoApiClient.reverseGeocode(lat, lng)).thenReturn(new GeocodingResult[]{geocodingResult});

    Address result = mapService.reverseGeocodeAddress(lat, lng);
    assertThat(result.getAddress(), is(address));
    assertThat(result.getZipCode(), is(zipCode));
  }
}
