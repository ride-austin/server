package com.rideaustin.rest.editors;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SafeLongEditorTest {

  public static final long DEFAULT_VALUE = 5L;
  private SafeLongEditor testedInstance;

  @Before
  public void setUp() {
    testedInstance = new SafeLongEditor(DEFAULT_VALUE);
  }

  @Test
  public void testSetAsText() throws Exception {
    testedInstance.setAsText("d");

    assertEquals(DEFAULT_VALUE, testedInstance.getValue());
  }

  @Test
  public void testSetAsTextSkipsNumeric() {
    testedInstance.setAsText("123");

    assertEquals(123L, testedInstance.getValue());
  }

}