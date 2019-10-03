package com.rideaustin.assemblers;

public interface DTOEnricher<D> extends SingleSideAssembler<D, D> {

  D enrich(D source);

  @Override
  default D toDto(D d) {
    return enrich(d);
  }
}
