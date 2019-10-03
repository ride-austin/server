package com.rideaustin.test.actions;

import static com.rideaustin.test.util.TestUtils.authorization;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.Api;
import com.rideaustin.test.util.TestUtils;

public abstract class AbstractAction<T> {

  protected final MockMvc mockMvc;
  protected final ObjectMapper mapper;

  public AbstractAction(MockMvc mockMvc, ObjectMapper mapper) {
    this.mockMvc = mockMvc;
    this.mapper = mapper;
  }

  protected T getRideInfo(String login, long id, Class<T> responseClass) throws Exception {
    return TestUtils.getResponseData(
      mockMvc,
      mapper,
      get(Api.RIDES + id)
        .param("avatarType", getAvatarType())
        .contentType(MediaType.APPLICATION_JSON)
        .headers(authorization(login)),
      responseClass
    );
  }

  public ResultActions updateRideDestination(String login, long id, LatLng destination) throws Exception {
    return mockMvc.perform(put(Api.RIDES + id)
      .headers(authorization(login))
      .param("endLocationLat", String.valueOf(destination.lat))
      .param("endLocationLong", String.valueOf(destination.lng))
      .param("avatarType", getAvatarType())
    );
  }

  protected abstract String getAvatarType();

}
