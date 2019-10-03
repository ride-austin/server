package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

@RunWith(MockitoJUnitRunner.class)
public class MapsHelperTest {

  private static final LatLng ANY_POINT = new LatLng(30.202670, -97.667000);

  @InjectMocks
  private MapsHelper mapsHelper;

  @Test
  public void shouldNotReduce() {
    // given
    List<LatLng> path = Arrays.asList(
      new LatLng(30.202666, -97.667003),
      new LatLng(30.202890, -97.664950),
      new LatLng(30.204672, -97.663292)
    );

    // when
    List<LatLng> result = mapsHelper.reduce(path);

    // then
    assertEquals(3, result.size());
  }

  @Test
  public void shouldReduce() {
    // given
    List<LatLng> path = Arrays.asList(
      new LatLng(30.202666, -97.667003),
      new LatLng(30.202690, -97.665570),
      new LatLng(30.202890, -97.664950)
    );

    // when
    List<LatLng> result = mapsHelper.reduce(path);

    // then
    assertEquals(2, result.size());
  }

  @Test
  public void shouldReduce2() {
    // given
    List<LatLng> path = Arrays.asList(
      new LatLng(30.21078000, -97.66120000),
      new LatLng(30.21187000, -97.65934000),
      new LatLng(30.21349640, -97.65831600)
    );

    // when
    List<LatLng> result = mapsHelper.reduce(path);

    // then
    assertEquals(2, result.size());
  }

  @Test
  public void shouldReduce3() {
    // given
    List<LatLng> path = Arrays.asList(
      new LatLng(30.20915000, -97.66256000),
      new LatLng(30.21078000, -97.66120000),
      new LatLng(30.21187000, -97.65934000)
    );

    // when
    List<LatLng> result = mapsHelper.reduce(path);

    // then
    assertEquals(2, result.size());
  }

  @Test
  public void shouldReduceForVeryBigAccuracy() {
    // given
    List<LatLng> path = Arrays.asList(
      ANY_POINT,
      new LatLng(210.21078000, 80.0),
      ANY_POINT
    );

    // when
    List<LatLng> result = mapsHelper.reduce(path, Double.MAX_VALUE);

    // then
    assertEquals(2, result.size());
    assertEquals(ANY_POINT, result.get(0));
    assertEquals(ANY_POINT, result.get(1));
  }

  @Test
  public void shouldGetPointsBetween() {
    // given
    DirectionsStep step = new DirectionsStep();
    step.startLocation = ANY_POINT;
    step.endLocation = ANY_POINT;
    step.polyline = new EncodedPolyline("u}iwDvqbsQ?}@?C?AAa@?e@?C?A?S?W?G?A?g@?WAM?G?GAQCMCQEQCMIOIQGKIKUUq@o@}FiFKC");

    // when
    List<LatLng> result = mapsHelper.toPoints(step);

    // then
    assertEquals(32, result.size());
    assertEquals(ANY_POINT, result.get(0));
    assertEquals(ANY_POINT, result.get(31));
  }

  @Test
  public void shouldGetAndReducePointsBetween() {
    // given
    DirectionsStep step = new DirectionsStep();
    step.startLocation = new LatLng(30.2026658, -97.6670034);
    step.endLocation = new LatLng(30.2046719, -97.6632917);
    step.polyline = new EncodedPolyline("u}iwDvqbsQ?}@?C?AAa@?e@?C?A?S?W?G?A?g@?WAM?G?GAQCMCQEQCMIOIQGKIKUUq@o@}FiFKC");

    // when
    List<LatLng> result = mapsHelper.toPoints(step);
    result = mapsHelper.reduce(result);

    // then
    assertTrue(result.size() == 3);
    assertEquals(step.startLocation, result.get(0));
    assertEquals(step.endLocation, result.get(2));
  }
}