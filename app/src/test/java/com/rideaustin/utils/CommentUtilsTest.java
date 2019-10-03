package com.rideaustin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CommentUtilsTest {

  @Test
  public void isReadableReturnsTrueForLongEnglishComment() throws Exception {

    boolean result = CommentUtils.isReadable("Great trip! Many thanks to Justin, he was an excellent driver!");

    assertTrue(result);
  }

  @Test
  public void isReadableReturnsTrueForShortEnglishComment() throws Exception {

    boolean result = CommentUtils.isReadable("Great!");

    assertTrue(result);
  }

  @Test
  public void isReadableReturnsTrueForSenselessNoisyComment() throws Exception {

    boolean result = CommentUtils.isReadable("fgkhanv. kfjvb!");

    assertTrue(result);
  }

  @Test
  public void isReadableReturnsFalseForCyrillicComment() throws Exception {

    boolean result = CommentUtils.isReadable("Спасибо, отличная поездка!");

    assertFalse(result);
  }

}