package com.rideaustin.rest.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.Range;

import com.google.common.collect.ImmutableSet;

public enum HeadingDirection {

  N {
    @Override
    protected Set<Range<Double>> ranges() {
      return ImmutableSet.of(Range.between(337.5, 360d), Range.between(0d, 22.5));
    }
  },
  NE {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(22.5, 67.5));
    }
  },
  E {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(67.5, 112.5));
    }
  },
  SE {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(112.5, 157.5));
    }
  },
  S {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(157.5, 202.5));
    }
  },
  SW {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(202.5, 247.5));
    }
  },
  W {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(247.5, 292.5));
    }
  },
  NW {
    @Override
    protected Set<Range<Double>> ranges() {
      return Collections.singleton(Range.between(292.5, 337.5));
    }
  };

  protected abstract Set<Range<Double>> ranges();

  public static HeadingDirection from(Double heading) {
    return Arrays.stream(values())
      .filter(h -> h.ranges().stream().anyMatch(r -> r.contains(heading)))
      .findFirst().orElse(null);
  }
}
