package com.rideaustin.assemblers;

import java.util.List;
import java.util.stream.Collectors;

public interface BilateralAssembler<I, O> extends SingleSideAssembler<I, O> {

  I toDs(O dto);

  default List<I> toDs(List<O> dto) {
    return dto.stream().map(this::toDs).collect(Collectors.toList());
  }

}
