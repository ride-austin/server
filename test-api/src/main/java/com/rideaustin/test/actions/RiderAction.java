package com.rideaustin.test.actions;

import static com.rideaustin.test.util.TestUtils.authorization;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.Api;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.rest.model.FarePaymentDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.test.response.EstimateFareDto;
import com.rideaustin.test.response.TestRideDto;
import com.rideaustin.test.util.TestUtils;

public class RiderAction extends AbstractAction<MobileRiderRideDto> {

  public RiderAction(MockMvc mockMvc, ObjectMapper mapper) {
    super(mockMvc, mapper);
  }

  public Long requestRide(String login, String googlePlaceId, LatLng location) throws Exception {
    return requestRide(login, googlePlaceId, location, null, TestUtils.REGULAR, null, null, false, Constants.DEFAULT_CITY_ID);
  }

  public Long requestRide(String login, LatLng location) throws Exception {
    return requestRide(login, location, TestUtils.REGULAR, null);
  }

  public Long requestRide(String login, LatLng startLocation, LatLng endLocation) throws Exception {
    return requestRide(login, startLocation, endLocation, TestUtils.REGULAR, null, null);
  }

  public Long requestRide(String login, LatLng startLocation, LatLng endLocation, boolean useSurgeFare) throws Exception {
    return requestRide(login, startLocation, endLocation, TestUtils.REGULAR, null, null, useSurgeFare, Constants.DEFAULT_CITY_ID);
  }

  public Long requestRide(String login, LatLng location, String carCategory) throws Exception {
    return requestRide(login, location, carCategory, null, null);
  }

  public Long requestRide(String login, LatLng location, String carCategory, long cityId) throws Exception {
    return requestRide(login, location, null, carCategory, null, null, false, cityId);
  }

  public Long requestRide(String login, LatLng location, String carCategory, String... driverTypes) throws Exception {
    return requestRide(login, location, carCategory, Optional.ofNullable(driverTypes).map(Arrays::asList).map(c -> String.join(",", c)).orElse(null), null);
  }

  public Long requestRide(String login, LatLng location, String carCategory, String driverType, String applePayToken) throws Exception {
    return requestRide(login, location, null, carCategory, driverType, applePayToken);
  }

  public Long requestRide(String login, LatLng location, LatLng endLocation, String carCategory, String driverType, String applePayToken) throws Exception {
    return requestRide(login, location, endLocation, carCategory, driverType, applePayToken, false, Constants.DEFAULT_CITY_ID);
  }

  public Long requestRide(String login, LatLng location, LatLng endLocation, String carCategory, String driverType, String applePayToken, boolean useSurgeFare, Long cityId) throws Exception {
    String response = performRideRequest(login, location, endLocation, carCategory, driverType, applePayToken, useSurgeFare, cityId)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return mapper.readValue(response, TestRideDto.class).getId();
  }

  public Long requestRide(String login, String googlePlaceId, LatLng location, LatLng endLocation, String carCategory,
    String driverType, String applePayToken, boolean useSurgeFare, Long cityId) throws Exception {
    String response = performRideRequest(login, googlePlaceId, location, endLocation, carCategory, driverType, applePayToken, useSurgeFare, cityId)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return mapper.readValue(response, TestRideDto.class).getId();
  }

  public EstimateFareDto estimateFare(String login, LatLng startLocation, LatLng endLocation, String carCategory, Long cityId) throws Exception {
    String response = mockMvc.perform(
      get(Api.RIDES + "estimate")
        .headers(authorization(login))
        .param("startLat", String.valueOf(startLocation.lat))
        .param("startLong", String.valueOf(startLocation.lng))
        .param("endLat", String.valueOf(endLocation.lat))
        .param("endLong", String.valueOf(endLocation.lng))
        .param("carCategory", carCategory)
        .param("cityId", String.valueOf(cityId))
    )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return mapper.readValue(response, EstimateFareDto.class);
  }

  public ResultActions cancelRide(String login, Long ride) throws Exception {
    final MvcResult result = mockMvc.perform(
      delete(Api.RIDES + ride)
        .headers(authorization(login))
        .param("avatarType", getAvatarType())
    ).andReturn();
    return mockMvc.perform(asyncDispatch(result));
  }

  public ResultActions acceptRideUpgrade(String login) throws Exception {
    return mockMvc.perform(
      post(Api.RIDE_UPGRADES + "accept")
        .headers(authorization(login))
    );
  }

  public ResultActions declineRideUpgrade(String login) throws Exception {
    return mockMvc.perform(
      post(Api.RIDE_UPGRADES + "decline")
        .param("avatarType", getAvatarType())
        .headers(authorization(login))
    );
  }

  public ResultActions rateRide(String login, long rideId, BigDecimal rating) throws Exception {
    return rateRide(login, rideId, rating, BigDecimal.ZERO, StringUtils.EMPTY);
  }

  public ResultActions rateRide(String login, long rideId, BigDecimal rating, BigDecimal tip, String comment) throws Exception {
    return rateRide(login, rideId, rating, tip, comment, PaymentProvider.CREDIT_CARD);
  }

  public ResultActions rateRide(String login, long rideId, BigDecimal rating, BigDecimal tip, String comment, PaymentProvider paymentProvider) throws Exception {
    return mockMvc.perform(
      put(String.format("%s/%s/rating", Api.RIDES, rideId))
        .param("rating", rating.toString())
        .param("tip", tip.toString())
        .param("comment", comment)
        .param("paymentProvider", paymentProvider.name())
        .headers(authorization(login))
    );
  }

  public ResultActions updateDestination(String login, long rideId, LatLng location) throws Exception {
    final MvcResult mvcResult = mockMvc.perform(
      put(String.format("%s/%s", Api.RIDES, rideId))
        .param("endLocationLat", String.valueOf(location.lat))
        .param("endLocationLong", String.valueOf(location.lng))
        .headers(authorization(login))
    )
      .andReturn();
    return mockMvc.perform(asyncDispatch(mvcResult));
  }

  public ResultActions updateRider(String login, long riderId, Rider rider) throws Exception {
    return mockMvc.perform(put(String.format("%s%s", Api.RIDERS, riderId))
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .content(mapper.writeValueAsString(rider))
    );
  }

  public ResultActions addDriver(String login, long termId, Driver driver) throws Exception {
    String licenseStr = "license";
    MockMultipartFile license = new MockMultipartFile("licenseData", licenseStr.getBytes(StandardCharsets.UTF_8));
    String insuranceStr = "insurance";
    MockMultipartFile insurance = new MockMultipartFile("insuranceData", insuranceStr.getBytes(StandardCharsets.UTF_8));
    MockMultipartHttpServletRequestBuilder request = fileUpload(Api.DRIVERS)
      .file(new MockMultipartFile("driver", "", MediaType.APPLICATION_JSON_VALUE,
        mapper.writeValueAsBytes(driver)));
    request.file(license);
    request.file(insurance);

    return mockMvc.perform(request
      .headers(authorization(login))
      .param("acceptedTermId", String.valueOf(termId)));
  }

  public ResultActions requestPhoneVerificationCode(String email, String phoneNumber) throws Exception {
    return mockMvc.perform(post(String.format("%s/%s", Api.PHONE_VERIFICATION, "requestCode"))
      .headers(authorization(email))
      .param("phoneNumber", phoneNumber));
  }

  public String getClosestActiveDriver(String login, LatLng location, String carCategory) throws Exception {
    return mockMvc.perform(
      get(Api.ACDR)
        .headers(authorization(login))
        .param("latitude", String.valueOf(location.lat))
        .param("longitude", String.valueOf(location.lng))
        .param("avatarType", getAvatarType())
        .param("carCategory", carCategory)
    )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  public ResultActions usePromocode(Rider rider, Promocode promocode) throws Exception {
    return mockMvc.perform(post(String.format("%s%s/promocode", Api.RIDERS, rider.getId()))
      .headers(authorization(rider.getEmail()))
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .content(mapper.writeValueAsString(promocode)));
  }

  public Long requestSplitFare(String login, Long rideId, Set<String> phoneNumbers) throws Exception {
    SplitFareDto splitFareDto = TestUtils.getResponseData(mockMvc, mapper, post(String.format("%s%d", Api.SPLIT_FARES, rideId))
      .headers(authorization(login))
      .param("phoneNumbers", String.join(",", phoneNumbers)), SplitFareDto.class);
    return splitFareDto.getId();
  }

  public ResultActions acceptSplitFare(String login, Long splitId) throws Exception {
    return mockMvc.perform(
      post(String.format("%s%d/accept", Api.SPLIT_FARES, splitId))
        .headers(authorization(login))
        .param("acceptance", Boolean.TRUE.toString())
    );
  }

  public MobileRiderRideDto getRideInfo(String login, long id) throws Exception {
    return getRideInfo(login, id, MobileRiderRideDto.class);
  }

  public List<FarePaymentDto> requestFarePaymentsInfo(String login, Long ride) throws Exception {
    return TestUtils.getResponseData(mockMvc, mapper,
      get(String.format("%s%d/list", Api.FARE_PAYMENTS, ride))
        .headers(authorization(login)),
      new TypeReference<List<FarePaymentDto>>() {
      });
  }

  public ResultActions deleteCard(String login, long riderId, long cardId) throws Exception {
    return mockMvc.perform(
      delete(String.format("%s%s/cards/%s", Api.RIDERS, riderId, cardId))
        .headers(authorization(login))
    );
  }

  public ResultActions updateCard(String login, long riderId, Long cardId) throws Exception {
    return mockMvc.perform(
      put(String.format("%s%s/cards/%s", Api.RIDERS, riderId, cardId))
        .headers(authorization(login))
        .param("riderId", String.valueOf(riderId))
        .param("cardId", String.valueOf(cardId))
        .param("primary", String.valueOf(true))
    );
  }

  public ResultActions associateRide(String login, String token) throws Exception {
    return mockMvc.perform(
      post(String.format("%squeue/%s", Api.RIDES, token))
        .headers(authorization(login))
    );
  }

  public ResultActions performRideRequest(String login, LatLng startLocation, String carType) throws Exception {
    return performRideRequest(login, startLocation, null, carType, null, null, false, Constants.DEFAULT_CITY_ID);
  }

  protected ResultActions performRideRequest(String login, LatLng location, LatLng endLocation, String carCategory,
    String driverType, String applePayToken, boolean useSurgeFare, long cityId) throws Exception {
    return mockMvc.perform(
      post(Api.RIDES)
        .headers(authorization(login))
        .param("startLocationLat", String.valueOf(location.lat))
        .param("startLocationLong", String.valueOf(location.lng))
        .param("endLocationLat", endLocation == null ? null : String.valueOf(endLocation.lat))
        .param("endLocationLong", endLocation == null ? null : String.valueOf(endLocation.lng))
        .param("carCategory", carCategory)
        .param("applePayToken", applePayToken)
        .param("driverType", driverType)
        .param("inSurgeArea", String.valueOf(useSurgeFare))
        .param("cityId", String.valueOf(cityId))
    );
  }

  protected ResultActions performRideRequest(String login, String startGooglePlaceId, LatLng location, LatLng endLocation,
    String carCategory, String driverType, String applePayToken, boolean useSurgeFare, long cityId) throws Exception {
    return mockMvc.perform(
      post(Api.RIDES)
        .headers(authorization(login))
        .param("startLocationLat", String.valueOf(location.lat))
        .param("startLocationLong", String.valueOf(location.lng))
        .param("startGooglePlaceId", startGooglePlaceId)
        .param("endLocationLat", endLocation == null ? null : String.valueOf(endLocation.lat))
        .param("endLocationLong", endLocation == null ? null : String.valueOf(endLocation.lng))
        .param("carCategory", carCategory)
        .param("applePayToken", applePayToken)
        .param("driverType", driverType)
        .param("inSurgeArea", String.valueOf(useSurgeFare))
        .param("cityId", String.valueOf(cityId))
    );
  }

  public List<CompactActiveDriverDto> searchDrivers(String login, LatLng location, String carCategory, String driverType) throws Exception {
    return TestUtils.getResponseData(mockMvc, mapper, get(Api.ACDR)
      .headers(authorization(login))
      .param("latitude", String.valueOf(location.lat))
      .param("longitude", String.valueOf(location.lng))
      .param("carCategory", carCategory)
      .param("driverType", driverType), new TypeReference<List<CompactActiveDriverDto>>() {
    });
  }

  public ResultActions setPrimaryCard(Rider rider) throws Exception {
    return setPrimaryCard(rider, rider.getPrimaryCard());
  }

  public ResultActions setPrimaryCard(Rider rider, RiderCard card) throws Exception {
    return mockMvc.perform(
      put(String.format("%s%d/cards/%d", Api.RIDERS, rider.getId(), card.getId()))
        .headers(authorization(rider.getEmail()))
        .param("primary", "true")
    );
  }

  public ResultActions getPendingPayments(Rider rider) throws Exception {
    return mockMvc.perform(
      get(String.format("%s%d/payments/pending", Api.RIDERS, rider.getId()))
        .headers(authorization(rider.getEmail()))
    );
  }

  public ResultActions payBalance(Rider rider, Long ride) throws Exception {
    return payBalance(rider, ride, null);
  }

  public ResultActions payBalance(Rider rider, Long ride, String applePayToken) throws Exception {
    return mockMvc.perform(
      post(String.format("%s%d/payments/pending", Api.RIDERS, rider.getId()))
        .headers(authorization(rider.getEmail()))
        .param("rideId", String.valueOf(ride))
        .param("applePayToken", applePayToken)
    );
  }

  @Override
  protected String getAvatarType() {
    return AvatarType.NAME_RIDER;
  }
}



