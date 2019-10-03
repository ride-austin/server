package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.rideaustin.rest.exception.ServerError;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DirectionsApi.class, DirectionsApiRequest.class})
public class GeoApiClientGoogleImplTest {

  private static final LatLng FROM = new LatLng(30.202596, -97.667001);
  private static final LatLng TO = new LatLng(30.213537, -97.658422);
  private static final List<LatLng> TWO_POINTS = new LinkedList<>(Arrays.asList(new LatLng(0d, 0d), new LatLng(0d, 0d)));
  private static final List<LatLng> THREE_POINTS = new LinkedList<>(Arrays.asList(new LatLng(0d, 0d), new LatLng(0d, 0d), new LatLng(0d, 0d)));
  private static final List<LatLng> FOUR_POINTS = new LinkedList<>(Arrays.asList(new LatLng(0d, 0d), new LatLng(0d, 0d), new LatLng(0d, 0d), new LatLng(0d, 0d)));

  @InjectMocks
  private GeoApiClientGoogleImpl clientGoogle;

  @Mock
  private MapsHelper mapsHelper;

  @Mock
  private Environment env;

  @Captor
  private ArgumentCaptor<List<LatLng>> captor;

  private ObjectMapper objectMapper;

  @Before
  public void init() {
    clientGoogle = new GeoApiClientGoogleImpl(env, mapsHelper, new GeoApiContext());
    objectMapper = createObjectMapper();
  }

  @Test
  public void shouldGetGoogleMiddlePointsForRealData() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue(new File("src/test/resources/directions-api-response.json"), DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    verify(mapsHelper, times(9)).toPoints(any());
    verify(mapsHelper, times(1)).reduce(captor.capture());
    assertEquals(308, captor.getValue().size());
    assertEquals(8, res.size());
    assertNotEquals(FROM, res.get(0));
    assertNotEquals(TO, res.get(7));
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForNullResponse() throws Exception {
    // given
    prepareMockResponse(null);

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForEmptyResponse() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue("{}", DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForEmptyRoutes() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue("{\"routes\": []}", DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForNullLegs() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue("{\"routes\": [{}]}", DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForEmptyLegs() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue("{\"routes\": [{\"legs\": []}]}", DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForNullSteps() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue("{\"routes\": [{\"legs\": [{}]}]}", DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForEmptySteps() throws Exception {
    // given
    prepareMockResponse(objectMapper.readValue("{\"routes\": [{\"legs\": [{\"steps\": []}]}]}", DirectionsResult.class));

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldNotGetGoogleMiddlePointsForException() throws Exception {
    // given
    PowerMockito.mockStatic(DirectionsApi.class);
    DirectionsApiRequest mockRequest = PowerMockito.mock(DirectionsApiRequest.class);
    when(DirectionsApi.newRequest(any(GeoApiContext.class))).thenReturn(mockRequest);
    when(mockRequest.origin(any(LatLng.class))).thenReturn(mockRequest);
    when(mockRequest.destination(any(LatLng.class))).thenReturn(mockRequest);
    when(mockRequest.mode(any(TravelMode.class))).thenReturn(mockRequest);
    when(mockRequest.await()).thenThrow(IllegalStateException.class);

    // when
    List<LatLng> res = clientGoogle.getGoogleMiddlePoints(FROM, TO);

    // then
    assertNoResponse(res);
  }

  @Test
  public void shouldCreateMapUrl() throws ServerError {
    // given
    List<LatLng> points = TWO_POINTS;
    Mockito.when(mapsHelper.polylineEncodedPath(points)).thenReturn("path");

    // when
    URI result = clientGoogle.getMapUrl(points, GeoApiClientGoogleImpl.MAP_SCALE);

    // then
    assertNotNull(result);
    verify(mapsHelper, times(0)).reduce(any());
    verify(mapsHelper, times(1)).polylineEncodedPath(points);
  }

  @Test
  public void shouldCreateMapUrlForReducedPoints() throws ServerError {
    // given
    Mockito.when(mapsHelper.polylineEncodedPath(THREE_POINTS)).thenReturn(randomString(2001));
    Mockito.when(mapsHelper.polylineEncodedPath(TWO_POINTS)).thenReturn(randomString(2000));
    Mockito.when(mapsHelper.reduce(THREE_POINTS, MapsHelper.ACCURACY_DECREMENT[0])).thenReturn(TWO_POINTS);

    // when
    URI result = clientGoogle.getMapUrl(THREE_POINTS, GeoApiClientGoogleImpl.MAP_SCALE);

    // then
    assertNotNull(result);
    verify(mapsHelper, times(1)).reduce(THREE_POINTS, MapsHelper.ACCURACY_DECREMENT[0]);
    verify(mapsHelper, times(3)).polylineEncodedPath(any());
  }

  @Test
  public void shouldCreateMapUrlForTwiceReducedPoints() throws ServerError {
    // given
    Mockito.when(mapsHelper.polylineEncodedPath(FOUR_POINTS)).thenReturn(randomString(2001));
    Mockito.when(mapsHelper.polylineEncodedPath(THREE_POINTS)).thenReturn(randomString(2001));
    Mockito.when(mapsHelper.polylineEncodedPath(TWO_POINTS)).thenReturn(randomString(2000));
    Mockito.when(mapsHelper.reduce(FOUR_POINTS, MapsHelper.ACCURACY_DECREMENT[0])).thenReturn(THREE_POINTS);
    Mockito.when(mapsHelper.reduce(THREE_POINTS, MapsHelper.ACCURACY_DECREMENT[1])).thenReturn(TWO_POINTS);

    // when
    URI result = clientGoogle.getMapUrl(FOUR_POINTS, GeoApiClientGoogleImpl.MAP_SCALE);

    // then
    assertNotNull(result);
    verify(mapsHelper, times(1)).reduce(FOUR_POINTS, MapsHelper.ACCURACY_DECREMENT[0]);
    verify(mapsHelper, times(1)).reduce(THREE_POINTS, MapsHelper.ACCURACY_DECREMENT[1]);
    verify(mapsHelper, times(4)).polylineEncodedPath(any());
  }

  private String randomString(int length) {
    return new BigDecimal(10).pow(length - 1).toString();
  }

  private void assertNoResponse(List<LatLng> res) {
    assertTrue(res.isEmpty());
    verifyZeroInteractions(mapsHelper);
  }

  private void prepareMockResponse(DirectionsResult directionsResult) throws Exception {
    PowerMockito.mockStatic(DirectionsApi.class);
    DirectionsApiRequest mockRequest = PowerMockito.mock(DirectionsApiRequest.class);
    Mockito.when(DirectionsApi.newRequest(any(GeoApiContext.class))).thenReturn(mockRequest);
    Mockito.when(mockRequest.await()).thenReturn(directionsResult);
    Mockito.when(mockRequest.origin(any(LatLng.class))).thenReturn(mockRequest);
    Mockito.when(mockRequest.destination(any(LatLng.class))).thenReturn(mockRequest);
    Mockito.when(mockRequest.mode(any(TravelMode.class))).thenReturn(mockRequest);
    Mockito.when(mapsHelper.toPoints(any())).thenCallRealMethod();
    Mockito.when(mapsHelper.reduce(any())).thenCallRealMethod();
    Mockito.when(mapsHelper.reduce(any(), eq(MapsHelper.ACCURACY_DECREMENT[0]))).thenCallRealMethod();
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
      .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
      .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
      .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
      .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    registerEnumToUpperDeserializer(objectMapper);
    return objectMapper;
  }

  private void registerEnumToUpperDeserializer(ObjectMapper objectMapper) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(LatLng.class, new LatLngDeserializer());
    module.addDeserializer(EncodedPolyline.class, new PolylineDeserializer());
    module.setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config, final JavaType type, BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
        return new JsonDeserializer<Enum>() {
          @Override
          public Enum deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
            return Enum.valueOf(rawClass, jp.getValueAsString().toUpperCase());
          }
        };
      }
    });
    objectMapper.registerModule(module);
  }

  private class LatLngDeserializer extends StdDeserializer<LatLng> {
    public LatLngDeserializer() {
      this(null);
    }

    public LatLngDeserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public LatLng deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode node = jp.getCodec().readTree(jp);
      double lat = (Double) node.get("lat").numberValue();
      double lng = (Double) node.get("lng").numberValue();
      return new LatLng(lat, lng);
    }
  }

  private class PolylineDeserializer extends StdDeserializer<EncodedPolyline> {
    public PolylineDeserializer() {
      this(null);
    }

    public PolylineDeserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public EncodedPolyline deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode node = jp.getCodec().readTree(jp);
      String points = node.get("points").asText();
      return new EncodedPolyline(points);
    }
  }
}