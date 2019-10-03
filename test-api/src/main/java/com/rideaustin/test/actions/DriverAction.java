package com.rideaustin.test.actions;

import static com.rideaustin.test.util.TestUtils.authorization;
import static com.rideaustin.test.util.TestUtils.getResponseData;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;
import com.rideaustin.Api;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.test.model.CachedEvent;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.utils.DateUtils;
import com.rideaustin.utils.RandomString;

public class DriverAction extends AbstractAction<MobileDriverRideDto> {

  public DriverAction(MockMvc mockMvc, ObjectMapper mapper) {
    super(mockMvc, mapper);
  }

  public ResultActions goOnline(String login, LatLng location) throws Exception {
    return mockMvc.perform(post(Api.ACDR)
      .headers(authorization(login))
      .param("latitude", String.valueOf(location.lat))
      .param("longitude", String.valueOf(location.lng))
    );
  }

  public ResultActions goOnline(String login, LatLng location, String[] carCategories) throws Exception {
    return mockMvc.perform(post(Api.ACDR)
      .headers(authorization(login))
      .param("latitude", String.valueOf(location.lat))
      .param("longitude", String.valueOf(location.lng))
      .param("carCategories", String.join(",", carCategories))
    );
  }

  public ResultActions goOnline(String login, double lat, double lng) throws Exception {
    return mockMvc.perform(post(Api.ACDR)
      .headers(authorization(login))
      .param("latitude", String.valueOf(lat))
      .param("longitude", String.valueOf(lng))
    );
  }

  public ResultActions locationUpdate(ActiveDriver activeDriver, double lat, double lng) throws Exception {
    return locationUpdate(activeDriver, lat, lng, new String[]{"REGULAR"});
  }

  public ResultActions locationUpdate(ActiveDriver activeDriver, double lat, double lng, String[] carCategories) throws Exception {
    return locationUpdate(activeDriver, lat, lng, carCategories, new String[0]);
  }

  public ResultActions locationUpdate(ActiveDriver activeDriver, double lat, double lng, String[] carCategories, String[] driverTypes) throws Exception {
    activeDriver.setLocationObject(null);
    return mockMvc.perform(put(Api.ACDR)
      .headers(authorization(activeDriver.getDriver().getEmail()))
      .param("latitude", String.valueOf(lat))
      .param("longitude", String.valueOf(lng))
      .param("carCategories", String.join(",", carCategories))
      .param("driverTypes", String.join(",", driverTypes))
    );
  }

  public ResultActions endRide(String login, long rideId, double lat, double lng) throws Exception {
    final MvcResult mvcResult = mockMvc.perform(
      post(String.format("%s%d/end", Api.RIDES, rideId))
        .headers(authorization(login))
        .param("endLocationLat", String.valueOf(lat))
        .param("endLocationLong", String.valueOf(lng))
        .param("endAddress", RandomString.generate())
        .param("endZipCode", RandomString.generate("1234657980", 5))
        .param("comment", "")
    ).andReturn();
    return mockMvc.perform(asyncDispatch(mvcResult));
  }

  public AreaQueuePositions getQueuePosition(Driver driver) throws Exception {
    return getResponseData(
      mockMvc,
      mapper,
      get(String.format(Api.QUEUE, driver.getId()))
        .headers(authorization(driver.getEmail())),
      AreaQueuePositions.class
    );
  }

  public AreaQueuePositions getQueueInfo(String login, String queue) throws Exception {
    return getResponseData(
      mockMvc,
      mapper,
      get(Api.BASE + "queues")
        .headers(authorization(login)),
      new TypeReference<List<AreaQueuePositions>>() {
      }
    ).stream()
      .filter(aqp -> aqp.getAreaQueueName().equalsIgnoreCase(queue))
      .findFirst()
      .orElse(null);
  }

  public ResultActions startRide(String login, long rideId) throws Exception {
    final MvcResult mvcResult = mockMvc.perform(
      post(String.format("%s%d/start", Api.RIDES, rideId))
        .headers(authorization(login))
    ).andReturn();
    return mockMvc.perform(asyncDispatch(mvcResult));
  }

  public ResultActions receivedRideRequest(String login, long id) throws Exception {
    return mockMvc.perform(
      post(String.format("%s%d/received", Api.RIDES, id))
        .headers(authorization(login))
    );
  }

  public ResultActions acceptRide(String login, long id) throws Exception {
    final MvcResult mvcResult = mockMvc.perform(
      post(String.format("%s%d/accept", Api.RIDES, id))
        .headers(authorization(login))
    ).andReturn();
    return mockMvc.perform(asyncDispatch(mvcResult));
  }

  public ResultActions acceptRide(ActiveDriver activeDriver, long id) throws Exception {
    final MvcResult mvcResult = mockMvc.perform(
      post(String.format("%s%d/accept", Api.RIDES, id))
        .headers(authorization(activeDriver.getDriver().getEmail()))
    ).andReturn();
    return mockMvc.perform(asyncDispatch(mvcResult));
  }

  public ResultActions reach(String login, long id) throws Exception {
    final MvcResult mvcResult = mockMvc.perform(
      post(String.format("%s%d/reached", Api.RIDES, id))
        .headers(authorization(login))
    ).andReturn();
    return mockMvc.perform(asyncDispatch(mvcResult));
  }

  public ResultActions goOffline(String login) throws Exception {
    return mockMvc.perform(
      delete(Api.ACDR)
        .headers(authorization(login))
    );
  }

  public ResultActions declineRide(String email, long rideId) throws Exception {
    return mockMvc.perform(
      delete(String.format("%s%d/decline", Api.RIDES, rideId))
        .headers(authorization(email))
    );
  }

  public ResultActions declineRide(ActiveDriver activeDriver, long rideId) throws Exception {
    return declineRide(activeDriver.getDriver().getEmail(), rideId);
  }

  public ResultActions requestRideUpgrade(String login, String target) throws Exception {
    return mockMvc.perform(
      post(Api.RIDE_UPGRADES + "request")
        .param("target", target)
        .headers(authorization(login))
    );
  }

  public ResultActions cancelRideUpgrade(String login) throws Exception {
    return mockMvc.perform(
      post(Api.RIDE_UPGRADES + "decline")
        .param("avatarType", getAvatarType())
        .headers(authorization(login))
    );
  }

  public MobileDriverRideDto getRideInfo(String login, long id) throws Exception {
    return getRideInfo(login, id, MobileDriverRideDto.class);
  }

  public ResultActions cancelRide(String login, long id) throws Exception {
    final MvcResult result = mockMvc.perform(
      delete(Api.RIDES + id)
        .param("avatarType", getAvatarType())
        .headers(authorization(login))
    ).andReturn();
    return mockMvc.perform(asyncDispatch(result));
  }

  public ResultActions rateRide(String login, long rideId, BigDecimal rating) throws Exception {
    return mockMvc.perform(
      put(String.format("%s/%s/rating", Api.RIDES, rideId))
        .param("rating", rating.toString())
        .headers(authorization(login))
    );
  }

  public ResultActions requestPhoneVerificationCode(String email, String phoneNumber) throws Exception {
    return mockMvc.perform(post(String.format("%s/%s", Api.PHONE_VERIFICATION, "requestCode"))
      .headers(authorization(email))
      .param("phoneNumber", phoneNumber));
  }

  public ResultActions requestEarningStats(Driver driver, Date start, Date end) throws Exception {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.systemDefault());
    return mockMvc.perform(get(String.format("%s%d/rides", Api.DRIVERS, driver.getId()))
      .headers(authorization(driver.getEmail()))
      .param("completedOnAfter", dateTimeFormatter.format(DateUtils.dateToInstant(start)))
      .param("completedOnBefore", dateTimeFormatter.format(DateUtils.dateToInstant(end)))
    );
  }

  public ResultActions requestDriverStats(Driver driver) throws Exception {
    return mockMvc.perform(get(String.format(Api.DRIVER_STATS, driver.getId()))
      .headers(authorization(driver.getEmail()))
    );
  }

  public ResultActions sendCached(String login, CachedEvent... events) throws Exception {
    Map<String, List<Map<String, String>>> payload = new HashMap<>();
    List<Map<String, String>> list = new ArrayList<>();
    for (CachedEvent event : events) {
      list.add(
        ImmutableMap.<String, String>builder()
          .put("rideId", String.valueOf(event.getRideId()))
          .put("eventType", event.getType().name())
          .put("eventTimestamp", String.valueOf(event.getTimestamp()))
          .putAll(event.getProperties())
          .build()
      );
    }
    payload.put("events", list);
    return mockMvc.perform(post(Api.RIDE_EVENTS)
      .content(mapper.writeValueAsString(payload))
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorization(login))
    );
  }

  @Override
  protected String getAvatarType() {
    return AvatarType.NAME_DRIVER;
  }
}
