package com.rideaustin.assemblers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface SingleSideAssembler<I, O> {

  O toDto(I ds);

  default List<O> toDto(Iterable<I> dss) {
    return StreamSupport.stream(dss.spliterator(), false).map(this::toDto).collect(Collectors.toList());
  }

}