package com.rideaustin.user.tracking.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserTrackDataTest {

  @Test
  public void isEmpty_True_When_All_Fields_Are_Absent() {
    UserTrackData tested = new UserTrackData();

    assertTrue(tested.isEmpty());
  }

  @Test
  public void isEmpty_False_When_At_Least_One_Is_Present() {
    UserTrackData tested = new UserTrackData();
    tested.setUtmSource("A");

    assertFalse(tested.isEmpty());
  }

  @Test
  public void isEmpty_False_When_All_Are_Present() {
    UserTrackData tested = new UserTrackData();
    tested.setUtmSource("A");
    tested.setUtmMedium("B");
    tested.setPromoCode("C");
    tested.setUtmCampaign("D");
    tested.setMarketingTitle("E");

    assertFalse(tested.isEmpty());
  }
}