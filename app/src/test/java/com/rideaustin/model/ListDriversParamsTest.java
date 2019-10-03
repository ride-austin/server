package com.rideaustin.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.rest.model.ListDriversParams;

public class ListDriversParamsTest {

  private ListDriversParams listDriversParams = new ListDriversParams();

  private BooleanBuilder booleanBuilder = new BooleanBuilder();

  @Test
  public void testWithMultipleParamsPart1() throws Exception {

    listDriversParams.setName("Maria Rodionova");
    listDriversParams.fill(booleanBuilder);
    assertMultipleQuery();
  }

  @Test
  public void testWithMultipleParamsPart2() throws Exception {

    listDriversParams.setName("Rodionova Maria");
    listDriversParams.fill(booleanBuilder);
    assertMultipleQuery();
  }

  @Test
  public void testWithSingleParam() throws Exception {

    listDriversParams.setName("Smith");
    listDriversParams.fill(booleanBuilder);
    assertThat(booleanBuilder.getValue().toString(),
      equalTo("containsIc(driver.user.firstname,Smith) || containsIc(driver.user.lastname,Smith) || containsIc(driver.user.firstname,Smith) || containsIc(driver.user.lastname,Smith)"));
  }

  private void assertMultipleQuery() {
    assertThat(booleanBuilder.getValue().toString(), containsString("containsIc(driver.user.firstname,Maria)"));
    assertThat(booleanBuilder.getValue().toString(), containsString("containsIc(driver.user.lastname,Rodionova"));
    assertThat(booleanBuilder.getValue().toString(), containsString("containsIc(driver.user.firstname,Rodionova) "));
    assertThat(booleanBuilder.getValue().toString(), containsString("containsIc(driver.user.lastname,Maria)"));
  }

}
