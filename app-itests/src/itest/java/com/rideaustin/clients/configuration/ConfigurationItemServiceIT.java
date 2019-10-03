package com.rideaustin.clients.configuration;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"dev","itest"})
@Category(ConfigurationItemChange.class)
@ContextConfiguration(classes = {AppConfig.class, TestConfig.class})
@WebAppConfiguration
public class ConfigurationItemServiceIT extends AbstractTransactionalJUnit4SpringContextTests {

  @Inject
  private ConfigurationItemService configurationItemService;

  @Inject
  private ConfigurationItemChangedEventInterceptor changedEventInterceptor;

  private String key = ConfigurationItemServiceIT.class.getSimpleName();

  private Map<String, Object> value = ImmutableMap.of("test", "value1");

  private ConfigurationItem item;

  private Long id;

  @Before
  public void before() throws Exception {
    item = configurationItemService.create(Constants.City.AUSTIN, ClientType.DRIVER, key, ImmutableMap.of("test", "value1"), true);
    id = item.getId();
    changedEventInterceptor.clear();
  }

  @After
  public void after() throws Exception {
    configurationItemService.remove(id);
  }

  @Test
  public void findAll() throws Exception {

    List<ConfigurationItem> items = configurationItemService.findAll();

    assertThat(items).isNotEmpty();
    assertThat(items).contains(item);
  }

  @Test
  public void findByKeyAndCityId() throws Exception {
    ConfigurationItem configurationItem = configurationItemService.findByKeyAndCityId(key, Constants.City.AUSTIN.getId());

    assertThat(configurationItem).isNotNull();
    assertThat(configurationItem).isEqualTo(item);
  }

  @Test
  public void findById() throws Exception {
    ConfigurationItem configurationItem = configurationItemService.findOne(id);

    assertThat(configurationItem).isNotNull();
    assertThat(configurationItem).isEqualTo(item);
  }

  @Test(expected = ConfigurationItemNotFoundException.class)
  public void findById_throws_not_found_exception() throws Exception {
    long id = new Random().nextLong();
    configurationItemService.findOne(-id);
  }

  @Test
  public void update() throws Exception {

    ConfigurationItem configurationItem = configurationItemService.findByKeyAndCityId(key, Constants.City.AUSTIN.getId());

    assertThat(configurationItem).isNotNull();
    ObjectMapper mapper = new ObjectMapper();
    assertThat(configurationItem.getConfigurationValue()).isEqualTo(mapper.writeValueAsString(value));

    configurationItemService.update(item, ImmutableMap.of("test", "value2"));

    configurationItem = configurationItemService.findByKeyAndCityId(key, Constants.City.AUSTIN.getId());


    TypeFactory typeFactory = mapper.getTypeFactory();
    MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
    Map<String, String> map = mapper.readValue(configurationItem.getConfigurationValue(), mapType);


    assertThat(configurationItem).isNotNull();
    assertThat(map).isEqualTo(ImmutableMap.of("test", "value2"));

    assertThat(changedEventInterceptor.getLastEvent()).isNotNull();
    assertThat(changedEventInterceptor.getLastEvent().getNewValue()).isEqualTo("{\"test\":\"value2\"}");
    assertThat(changedEventInterceptor.getLastEvent().getOldValue()).isEqualTo("{\"test\":\"value1\"}");
  }

  @Test
  public void update_same_value_does_not_publish_event() throws Exception {

    Map<String, Object> currentValue = ImmutableMap.of("test", "value1");

    ConfigurationItem configurationItem = configurationItemService.findByKeyAndCityId(key, Constants.City.AUSTIN.getId());

    assertThat(configurationItem).isNotNull();
    assertThat(configurationItem.getConfigurationValue()).isEqualTo("{\"test\":\"value1\"}");


    configurationItemService.update(item, currentValue);
    assertThat(changedEventInterceptor.getLastEvent()).isNull();
  }
}