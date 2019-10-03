package com.rideaustin.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.rideaustin.service.model.Version;
import com.rideaustin.utils.AppInfoUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class AppInfoUtilsTest {

  @DataProvider
  public static Object[] comparedVersions() {
    return new Object[]{
      ImmutableTriple.of("2.6.0", "2.7.0", -1),
      ImmutableTriple.of("2.7.0", "2.6.0", 1),
      ImmutableTriple.of("2.7.0", "2.7.0", 0),
      ImmutableTriple.of("3.1.0", "2.7.0", 1),
      ImmutableTriple.of("2.7.0", "3.1.0", -1),
      ImmutableTriple.of("2.7.0", "2.7.1", -1),
      ImmutableTriple.of("2.7.1", "2.7.0", 1)
    };
  }

  @Test
  public void testParseAppInfoAustinOK() throws Exception {
    assertThat(AppInfoUtils.extractVersion("RideAustin_iOS_2.6.0 (228)"), is("iOS 2.6.0 (228)"));
    assertThat(AppInfoUtils.extractVersion("RideAustinDriver_iOS_2.6.0b (233)"), is("iOS 2.6.0b (233)"));
  }

  @Test
  public void testParseAppInfoAustinFail() throws Exception {
    assertThat(AppInfoUtils.extractVersion("RideAustina_iOS_2.6.0 (228)"), is("RideAustina iOS 2.6.0 (228)"));
    assertThat(AppInfoUtils.extractVersion("RideAustinDrover_iOS_2.6.0b (233)"), is("RideAustinDrover iOS 2.6.0b (233)"));

  }

  @Test
  public void testParseAppInfoHoustonOk() throws Exception {
    assertThat(AppInfoUtils.extractVersion("RideHouston_iOS_2.6.0 (228)"), is("iOS 2.6.0 (228)"));
    assertThat(AppInfoUtils.extractVersion("RideHoustonDriver_iOS_2.6.0 (233)"), is("iOS 2.6.0 (233)"));
  }

  @Test
  public void testParseAppInfoHoustonFail() throws Exception {
    assertThat(AppInfoUtils.extractVersion("RideHauston_iOS_2.6.0 (228)"), is("RideHauston iOS 2.6.0 (228)"));
    assertThat(AppInfoUtils.extractVersion("RideHoustonDrsver_iOS_2.6.0 (233)"), is("RideHoustonDrsver iOS 2.6.0 (233)"));
  }

  @Test
  public void testCreateVersionOk() throws Exception {
    Version version = AppInfoUtils.createVersion("RideHouston_iOS_2.6.0 (228)");
    assertEquals(2, version.getMajor());
    assertEquals(6, version.getMinor());
    assertEquals(0, version.getHotfix());
  }

  @Test
  public void testCreateVersionFail() throws Exception {
    Version version = AppInfoUtils.createVersion("RideHoudton_iOS_2.60 (228)");
    assertEquals(0, version.getMajor());
    assertEquals(0, version.getMinor());
    assertEquals(0, version.getHotfix());
  }

  @Test
  @UseDataProvider("comparedVersions")
  public void testCompareVersion(ImmutableTriple<String, String, Integer> expected) {
    Version first = new Version(expected.getLeft());
    Version second = new Version(expected.getMiddle());

    assertEquals((int) expected.getRight(), first.compareTo(second));
  }

}
