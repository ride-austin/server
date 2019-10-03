package com.rideaustin.rest.model;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class HeadingDirectionTest {
  @Test
  public void testFromNorth() throws Exception {
    doTest(ImmutableSet.of(355.1, 0.0, 10.0), HeadingDirection.N);
  }

  @Test
  public void testFromNorthEast() throws Exception {
    doTest(ImmutableSet.of(40.0, 45.0, 50.5), HeadingDirection.NE);
  }

  @Test
  public void testFromEast() throws Exception {
    doTest(ImmutableSet.of(70.4, 90.1, 109.6), HeadingDirection.E);
  }

  @Test
  public void testFromSouthEast() throws Exception {
    doTest(ImmutableSet.of(114.4, 135.5, 153.1), HeadingDirection.SE);
  }

  @Test
  public void testFromSouth() throws Exception {
    doTest(ImmutableSet.of(202.1, 180.0, 161.1), HeadingDirection.S);
  }

  @Test
  public void testFromSouthWest() throws Exception {
    doTest(ImmutableSet.of(215.4, 225.1, 233.1), HeadingDirection.SW);
  }

  @Test
  public void testFromWest() throws Exception {
    doTest(ImmutableSet.of(254.4, 270.5, 283.1), HeadingDirection.W);
  }

  @Test
  public void testFromNorthWest() throws Exception {
    doTest(ImmutableSet.of(304.4, 315.5, 323.1), HeadingDirection.NW);
  }

  private void doTest(Set<Double> headings, HeadingDirection expected) {
    for (Double heading : headings) {
      assertEquals(expected, HeadingDirection.from(heading));
    }
  }
}