package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class PromocodeRedemptionExpirationJobIT extends ITestProfileSupport {

  public static final String QUERY_FOR_ACTIVE_STATUS = "select active from promocode_redemptions where id = ?";
  @Inject
  private PromocodeService promocodeService;

  @Inject
  @Named("invalidRedemption")
  private PromocodeRedemptionFixture invalidRedemptionFixture;
  private PromocodeRedemption invalidRedemption;

  @Inject
  @Named("validRedemption")
  private PromocodeRedemptionFixture validRedemptionFixture;
  private PromocodeRedemption validRedemption;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    invalidRedemption = invalidRedemptionFixture.getFixture();
    validRedemption = validRedemptionFixture.getFixture();
  }

  @Test
  public void testOnlyInvalidRedemptionsAreExpired() throws Exception {
    promocodeService.deactivateExpiredRedemptions();

    Boolean invalidIsActive = jdbcTemplate.queryForObject(QUERY_FOR_ACTIVE_STATUS, new Object[]{invalidRedemption.getId()}, Boolean.class);
    Boolean validIsActive = jdbcTemplate.queryForObject(QUERY_FOR_ACTIVE_STATUS, new Object[]{validRedemption.getId()}, Boolean.class);

    assertFalse("Expected invalid redemption to be deactivated", invalidIsActive);
    assertTrue("Expected valid redemption to remain active", validIsActive);
  }
}
