package com.rideaustin.clients.configuration;

import com.rideaustin.filter.ClientType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static com.rideaustin.test.util.TestUtils.authorization;

class ConfigurationItemsAdminAction {

  private final MockMvc mockMvc;


  ConfigurationItemsAdminAction(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }


  public ResultActions create(String login, ClientType clientType, String configurationKey, String configurationValue, boolean isDefault) throws Exception {

    String requestBody = "{" +
      "\"cityId\":1," +
      "\"clientType\":\"" + clientType.name() + "\", " +
      "\"default\":" + isDefault + ", " +
      "\"configurationKey\":\"" + configurationKey + "\", " +
      "\"configurationValue\":" + configurationValue + "" +
      "}";

    return mockMvc.perform(post("/rest/configs/items")
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON)
      .content(requestBody)
    );
  }

  public ResultActions remove(String login, Long itemId) throws Exception {

    return mockMvc.perform(delete("/rest/configs/items/{id}", itemId)
      .headers(authorization(login))
    );
  }

  public ResultActions update(String login, Long itemId, String requestBody) throws Exception {
    return mockMvc.perform(put("/rest/configs/items/{id}", itemId)
      .headers(authorization(login))
      .contentType(MediaType.APPLICATION_JSON)
      .content(requestBody)
    );
  }


}
