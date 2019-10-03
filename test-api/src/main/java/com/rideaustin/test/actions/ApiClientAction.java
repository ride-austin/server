package com.rideaustin.test.actions;

import static com.rideaustin.test.util.TestUtils.authorization;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.Api;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.model.DeeplinkDto;

public class ApiClientAction extends AbstractAction {

  public ApiClientAction(MockMvc mockMvc, ObjectMapper mapper) {
    super(mockMvc, mapper);
  }

  @Override
  protected String getAvatarType() {
    return AvatarType.NAME_API_CLIENT;
  }

  public String requestRide(String login, LatLng location, LatLng endLocation) throws Exception {
    String response = mockMvc.perform(
      post(Api.RIDES)
        .headers(authorization(login))
        .param("startLocationLat", String.valueOf(location.lat))
        .param("startLocationLong", String.valueOf(location.lng))
        .param("endLocationLat", endLocation == null ? null : String.valueOf(endLocation.lat))
        .param("endLocationLong", endLocation == null ? null : String.valueOf(endLocation.lng))
        .param("avatarType", AvatarType.NAME_API_CLIENT)
    )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return mapper.readValue(response, DeeplinkDto.class).getDeeplink();
  }
}
