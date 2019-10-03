package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.service.DefaultSearchDriverHandler.CompositeFilter;
import com.rideaustin.service.DefaultSearchDriverHandler.DriverFilter;
import com.rideaustin.service.model.OnlineDriverDto;

public class CompositeDriverFilterTest {

  @Mock
  private DriverFilter innerFilter;
  @Mock
  private DriverFilter innerFilter2;

  private CompositeFilter testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CompositeFilter(innerFilter);
  }

  @Test
  public void filterReturnsFalseOnFirstInnerFilterFalse() {
    final OnlineDriverDto driver = new OnlineDriverDto();
    when(innerFilter.filter(driver)).thenReturn(false);
    when(innerFilter2.filter(driver)).thenReturn(true);

    final boolean result = testedInstance.filter(driver);

    assertFalse(result);
  }

  @Test
  public void filterReturnsTrueOnAllInnerFilterTrue() {
    final OnlineDriverDto driver = new OnlineDriverDto();
    when(innerFilter.filter(driver)).thenReturn(true);
    when(innerFilter2.filter(driver)).thenReturn(true);

    final boolean result = testedInstance.filter(driver);

    assertTrue(result);
  }
}
