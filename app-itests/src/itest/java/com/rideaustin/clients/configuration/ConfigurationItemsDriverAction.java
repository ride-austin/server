package com.rideaustin.clients.configuration;

import java.util.Collections;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class ConfigurationItemsDriverAction {

  private final MockMvc mockMvc;


  public ConfigurationItemsDriverAction(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }


  public ResultActions getEvents(Driver driver) throws Exception {

    Authentication authentication = getAuthenticationToOvercomeFixtureProblemWithInfoUserId0(driver);

    return mockMvc.perform(get("/rest/events")
      .param("avatarType", AvatarType.DRIVER.name())
      .with(authentication(authentication)))
      .andDo(print());
  }

  public ResultActions getAsyncDispatch(MvcResult mvcResult) throws Exception {
    return this.mockMvc.perform(asyncDispatch(mvcResult));
  }


  /**
   * Info info = user.avatarInfo(avatarType);
   * info will contain id=0 (WRONG) and events subscription will not work,
   * see {@link com.rideaustin.service.event.EventManager}
   * see {@link com.rideaustin.rest.Events}
   */
  private Authentication getAuthenticationToOvercomeFixtureProblemWithInfoUserId0(Driver driver) {
    User user = driver.getUser();
    user.setAvatars(Collections.singletonList(driver));
    Authentication authentication = new TestingAuthenticationToken(user, "", AuthorityUtils.createAuthorityList(AvatarType.ROLE_DRIVER));
    authentication.setAuthenticated(true);
    return authentication;
  }

}
