package com.rideaustin.test.actions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.rideaustin.test.util.TestUtils.authorization;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.Api;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.rest.model.SurgeAreaDto;

import com.rideaustin.test.util.PageBean;
import com.rideaustin.test.util.TestUtils;

public class AdministratorAction extends AbstractAction {

  public AdministratorAction(MockMvc mockMvc, ObjectMapper mapper) {
    super(mockMvc, mapper);
  }

  public String getClosestActiveDriverForAdmin(String login, LatLng location, String carCategory) throws Exception {

    return mockMvc.perform(
      get(Api.ACDR)
        .headers(authorization(login))
        .param("startLocationLat", String.valueOf(location.lat))
        .param("startLocationLong", String.valueOf(location.lng))
        .param("avatarType", getAvatarType())
        .param("carCategory", carCategory)
    )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

  }

  public ResultActions updateRider(String login, long riderId, Rider rider) throws Exception {
    return mockMvc.perform(put(String.format("%s%s", Api.RIDERS, riderId))
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .content(mapper.writeValueAsString(rider))
    );
  }

  public ResultActions updateDriver(String login, long driverId, ConsoleDriverDto driver) throws Exception {
    return mockMvc.perform(put(String.format("%s/%s", Api.DRIVERS, driverId))
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .content(mapper.writeValueAsString(driver))
    );
  }

  public ResultActions updateSurgeFactor(String login, SurgeArea template, BigDecimal factor) throws Exception {
    RedisSurgeArea data = TestUtils.getResponsePage(mockMvc, mapper,
      get(Api.SURGE_AREAS)
        .param("name", template.getName())
        .headers(authorization(login)),
      new TypeReference<PageBean<RedisSurgeArea>>() {
      }
    ).get(0);
    Map<String, BigDecimal> surgeFactors = data.getSurgeMapping();
    for (String key : surgeFactors.keySet()) {
      surgeFactors.put(key, factor);
    }
    SurgeAreaDto surgeAreaDto = new SurgeAreaDto();
    BeanUtils.copyProperties(data, surgeAreaDto);
    BeanUtils.copyProperties(template, surgeAreaDto, "surgeFactors");
    BeanUtils.copyProperties(template.getAreaGeometry(), surgeAreaDto, "id");
    surgeAreaDto.setSurgeFactors(surgeFactors);
    surgeAreaDto.setName(template.getName());
    return mockMvc.perform(put(Api.SURGE_AREAS + template.getId())
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .content(mapper.writeValueAsString(surgeAreaDto))
    );
  }

  public ResultActions addPromocode(String login, PromocodeDto promocodeDto) throws Exception {
    return mockMvc.perform(post(Api.PROMOCODES)
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .content(mapper.writeValueAsString(promocodeDto)));
  }

  public ResultActions disableDriverPermanently(String login, Long driverId) throws Exception {
    return mockMvc.perform(delete(String.format("%s/%s/quickdisable", Api.DRIVERS, driverId))
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON_VALUE));
  }

  public ResultActions requestDriverStats(String login, long driverId) throws Exception {
    return mockMvc.perform(get(String.format(Api.DRIVER_STATS, driverId))
      .headers(authorization(login))
    );
  }

  @Override
  protected String getAvatarType() {
    return AvatarType.NAME_ADMIN;
  }
}