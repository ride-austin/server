package com.rideaustin.service.promocodes;

import com.rideaustin.testrail.TestCases;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(RiderPromocode.class)
public class C1266629IT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1266629")
  public void testApplicable() throws Exception {
    this.promocode = setup.getApplicablePromocode();
    doTest();
  }

  @Test
  @TestCases("C1266629")
  public void testNonApplicable() throws Exception {
    this.promocode = setup.getNonApplicablePromocode();
    doTest();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    Date start = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC));
    Date end = Date.from(Instant.ofEpochMilli(start.getTime()).plus(1, ChronoUnit.DAYS));
    driverAction.requestEarningStats(activeDriver.getDriver(), start, end)
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].driverPayment").value(is("15.58")));
  }

}
