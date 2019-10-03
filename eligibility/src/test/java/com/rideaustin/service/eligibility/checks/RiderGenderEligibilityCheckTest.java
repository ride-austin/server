package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.rideaustin.model.user.Gender;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RiderGenderEligibilityCheckTest {

  private RiderGenderEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new RiderGenderEligibilityCheck();
  }

  @DataProvider
  public static Object[] ineligibleGenders() {
    return EnumSet.complementOf(EnumSet.of(Gender.FEMALE)).toArray();
  }

  @Test
  @UseDataProvider("ineligibleGenders")
  public void checkRaisesErrorWhenRiderIsNotFemale(Gender gender) {
    final Rider subject = new Rider();
    final User user = new User();
    user.setGender(gender);
    subject.setUser(user);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals("Rider is not eligible to request Women Only ride", result.get().getMessage());
  }

  @Test
  public void checkRaisesNoErrorWhenRiderIsFemale() {
    final Rider subject = new Rider();
    final User user = new User();
    user.setGender(Gender.FEMALE);
    subject.setUser(user);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertFalse(result.isPresent());
  }
}