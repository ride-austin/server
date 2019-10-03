package com.rideaustin.clients.configuration;


import javax.inject.Inject;
import javax.inject.Named;

import com.jayway.jsonpath.JsonPath;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.event.EventManager;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.DriverFixture;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @see <a href="http://testrail.devfactory.com/index.php?/cases/view/C1254602">C1254602</a>
 */

@Category(ConfigurationItemChange.class)
@ITestProfile
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1254602IT extends AbstractTransactionalJUnit4SpringContextTests {

  @Inject
  protected AdministratorFixture administratorFixture;

  @Inject
  @Named("simpleDriver")
  private DriverFixture driverFixture;

  @Inject
  private ConfigurationItemsAdminAction configurationItemsAdminAction;

  @Inject
  private ConfigurationItemsDriverAction configurationItemsDriverAction;

  @Inject
  private DriverAction driverAction;

  @Inject
  private EventManager eventManager;

  private Long itemId;

  private String itemKey = C1254602IT.class.getSimpleName();

  private Administrator administrator;

  private Driver driver;


  @Before
  public void before() throws Exception {

    driver = driverFixture.getFixture();
    administrator = administratorFixture.getFixture();

    driverAction.goOnline(driver.getEmail(), 30.202596, -97.667001);
    String configurationValue = "{\"some\" : \"json\"}";

    administrator = administratorFixture.getFixture();
    String content = configurationItemsAdminAction.create(administrator.getEmail(), ClientType.DRIVER, itemKey, configurationValue, true)
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
  @TestCases("C1254602")
  public void create_new_driver_configuration_item_when_driver_is_ONLINE() throws Exception {

    MvcResult mvcResult = configurationItemsDriverAction.getEvents(driver)
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(request().asyncStarted())
      .andReturn();

    eventManager.refreshEvents();

    configurationItemsDriverAction.getAsyncDispatch(mvcResult)
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/json;charset=UTF-8"))
      .andExpect(jsonPath("$[0].eventType").value(equalTo(EventType.CONFIG_CREATED.name())))
      .andExpect(jsonPath("$[0].parameters").value(notNullValue()))
    ;
  }

}
