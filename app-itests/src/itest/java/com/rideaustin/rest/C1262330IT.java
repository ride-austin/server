package com.rideaustin.rest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static com.rideaustin.test.util.TestUtils.authorization;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.fixtures.DriverFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.testrail.TestCases;

@ActiveProfiles({"dev","itest"})
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1262330IT extends AbstractTransactionalJUnit4SpringContextTests {

  @Inject
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  @Named("simpleDriver")
  protected DriverFixture driverFixture;

  private ActiveDriver activeDriver;

  @Before
  public void setup() throws Exception {
    mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    activeDriver = activeDriverFixtureProvider.create(driverFixture).getFixture();
  }

  @Test
  @TestCases("C1262330")
  @WithMockUser(roles = AvatarType.NAME_DRIVER)
  public void test_Get_ride_when_driver_is_AVAILABLE_not_in_a_ride_and_offline() throws Exception {

    mockMvc.perform(
      get("/rest/rides/current")
        .param("avatarType", AvatarType.DRIVER.name())
        .headers(authorization(activeDriver.getDriver().getEmail())))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().string(""));
  }

}

