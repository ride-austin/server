package com.rideaustin.clients.configuration;


import javax.inject.Inject;
import java.util.Random;

import com.jayway.jsonpath.JsonPath;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.testrail.TestCases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @see <a href="http://testrail.devfactory.com/index.php?/cases/view/1254601">1254601</a>
 */
@Category(ConfigurationItemChange.class)
@ITestProfile
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1254601IT extends AbstractTransactionalJUnit4SpringContextTests {


  @Inject
  protected AdministratorFixture administratorFixture;

  @Inject
  private ConfigurationItemsAdminAction configurationItemsAdminAction;

  private Administrator administrator;

  private String itemKey = C1254601IT.class.getSimpleName();
  private Long itemId;

  @Before
  public void before() throws Exception {

    String configurationValue = "{\"some\" : \"json\"}";

    administrator = administratorFixture.getFixture();
    String content = configurationItemsAdminAction.create(administrator.getEmail(), ClientType.DRIVER, itemKey, configurationValue, true
    )
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("id").value(notNullValue()))
      .andReturn()
      .getResponse()
      .getContentAsString();

    Integer id = JsonPath.read(content, "id");
    itemId = id.longValue();
    assertThat(itemId).isPositive();
  }

  @After
  public void after() throws Exception {
    configurationItemsAdminAction.remove(administrator.getEmail(), itemId)
      .andDo(print())
      .andExpect(status().isOk());
  }

  @Test
  @TestCases("C1254601")
  public void update_with_invalid_json() throws Exception {
    configurationItemsAdminAction.update(administrator.getEmail(), itemId, "{")
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("invalid request body")))
    ;
  }

  @Test
  @TestCases("C1254601")
  public void update_with_invalid_json_empty_body() throws Exception {
    configurationItemsAdminAction.update(administrator.getEmail(), itemId, "")
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("invalid request body")))
    ;
  }

  @Test
  @TestCases("C1254601")
  public void update_with_invalid_json_empty_string_value() throws Exception {
    configurationItemsAdminAction.update(administrator.getEmail(), itemId, "{\"configurationValue\":\"\"}")
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("invalid configurationValue json")))
    ;
  }

  @Test
  @TestCases("C1254601")
  public void update_with_invalid_json_null_value() throws Exception {
    configurationItemsAdminAction.update(administrator.getEmail(), itemId, "{\"configurationValue\":null}")
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("may not be null")))
    ;
  }

  @Test
  @TestCases("C1254601")
  public void update_with_invalid_json_missing_value() throws Exception {
    configurationItemsAdminAction.update(administrator.getEmail(), itemId, "{}")
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("may not be null")))
    ;
  }

  @Test
  @TestCases("C1254601")
  public void update_with_not_existing_key() throws Exception {

    configurationItemsAdminAction.update(administrator.getEmail(), -(new Random().nextLong()), "{}")
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("may not be null")))
    ;
  }
}
