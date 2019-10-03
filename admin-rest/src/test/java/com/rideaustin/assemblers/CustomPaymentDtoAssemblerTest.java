package com.rideaustin.assemblers;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.enums.CustomPaymentCategory;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.CustomPaymentDto;
import com.rideaustin.utils.DateUtils;

public class CustomPaymentDtoAssemblerTest {

  private CustomPaymentDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new CustomPaymentDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final CustomPaymentDto result = testedInstance.toDto((CustomPayment) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    CustomPayment source = createCustomPayment();

    final CustomPaymentDto result = testedInstance.toDto(source);

    assertEquals(source.getDriver().getFirstname(), result.getDriverFirstName());
    assertEquals(source.getDriver().getLastname(), result.getDriverLastName());
    assertEquals(source.getDriver().getEmail(), result.getDriverEmail());
    assertEquals(source.getCreator().getFirstname(), result.getCreatorFirstName());
    assertEquals(source.getCreator().getLastname(), result.getCreatorLastName());
    assertEquals(source.getCreator().getEmail(), result.getCreatorEmail());
    assertEquals(source.getValue(), result.getValue());
    assertEquals(source.getDescription(), result.getDescription());
    assertEquals(source.getCategory(), result.getCategory());
  }

  @Test
  public void toDtoFillsDate() {
    CustomPayment source = createCustomPayment();
    source.setPaymentDate(DateUtils.localDateToDate(LocalDate.of(2019, 12, 31)));

    final CustomPaymentDto result = testedInstance.toDto(source);

    assertEquals("2019-12-31", result.getPaymentDate());
  }

  private CustomPayment createCustomPayment() {
    CustomPayment source = new CustomPayment();
    final Administrator creator = new Administrator();
    final User creatorUser = new User();
    creatorUser.setFirstname("A");
    creatorUser.setLastname("B");
    creatorUser.setEmail("C");
    creator.setUser(creatorUser);
    source.setCreator(creator);
    final Driver driver = new Driver();
    final User driverUser = new User();
    driverUser.setFirstname("D");
    driverUser.setLastname("E");
    driverUser.setEmail("F");
    driver.setUser(driverUser);
    source.setDriver(driver);
    source.setValue(money(10.0));
    source.setCategory(CustomPaymentCategory.BONUS);
    source.setDescription("Description");
    return source;
  }
}