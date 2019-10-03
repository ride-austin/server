package com.rideaustin.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.rideaustin.utils.PhoneNumberUtils;

@RunWith(JUnit4.class)
public class PhoneNumberUtilsTest {
  @Test
  public void testCleanPhoneNumber() throws Exception {
    assertThat(PhoneNumberUtils.cleanPhoneNumber("safasd123sadsf234"), is("123234"));
    assertThat(PhoneNumberUtils.cleanPhoneNumber(" !@#@--23-12 332"), is("2312332"));
    assertThat(PhoneNumberUtils.cleanPhoneNumber("  123 \n   234 sdf -- _++21?!"), is("12323421"));
    assertThat(PhoneNumberUtils.cleanPhoneNumber("!@#$%^&*(123456!@#$%U"), is("123456"));
  }

  @Test
  public void testOnlyLast10numbers() throws Exception {
    assertThat(PhoneNumberUtils.onlyLast10Numbers("123123123123132"), is("3123123132"));
    assertThat(PhoneNumberUtils.onlyLast10Numbers("ads fs234dfs 234"), is("234dfs 234"));
    assertThat(PhoneNumberUtils.onlyLast10Numbers("123123123 123ads fs234dfs2234"), is("234dfs2234"));
  }

  @Test
  public void testToBracketStandars() throws Exception {
    assertThat(PhoneNumberUtils.toBracketsStandard("123123123123132"), is("(312) 312-3132"));
    assertThat(PhoneNumberUtils.toBracketsStandard("ads fs234dfs 12312312123234"), is("(231) 212-3234"));
    assertThat(PhoneNumberUtils.toBracketsStandard(PhoneNumberUtils.cleanPhoneNumber("123123123 123ads fs234dfs2234")), is("(123) 234-2234"));
  }
}
