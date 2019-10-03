package com.rideaustin.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.rideaustin.utils.CryptUtils;

public class CryptUtilsTest {

  @Test
  public void shouldDecode_WhenEncodedTextIsGiven() {
    final String encodedText = "0YLQtdGB0YI=";
    final String expected = "тест";

    String actual = CryptUtils.base64Decode(encodedText);
    System.out.println(actual);

    assertEquals(actual, expected);
  }

  @Test
  public void shouldNotDecodeEmptyText() {
    final String text = StringUtils.EMPTY;
    final String expected = StringUtils.EMPTY;

    String actual = CryptUtils.base64Decode(text);

    assertEquals(actual, expected);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowException_WhenNonEncodedTextIsGiven() {
    final String nonEncodedText = "тест";

    CryptUtils.base64Decode(nonEncodedText);
    fail("Should have failed");
  }

  @Test
  public void shouldEncode(){
    final String text = "test";
    final String expected = "dGVzdA==";

    String actual = CryptUtils.encodeBase64(text);

    assertEquals(expected, actual);
  }

  @Test
  public void shouldNotEncodeEmptyText(){
    final String text = StringUtils.EMPTY;
    final String expected = StringUtils.EMPTY;

    String actual = CryptUtils.encodeBase64(text);

    assertEquals(expected, actual);
  }
}
