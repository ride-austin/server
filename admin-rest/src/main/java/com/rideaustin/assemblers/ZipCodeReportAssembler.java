package com.rideaustin.assemblers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.querydsl.core.Tuple;
import com.rideaustin.report.TupleConsumer;
import com.rideaustin.service.model.ZipCodeReportEntry;

@Component
public class ZipCodeReportAssembler implements SingleSideAssembler<Tuple, ZipCodeReportEntry>, TupleConsumer, Converter<Tuple, ZipCodeReportEntry> {

  @Override
  public ZipCodeReportEntry toDto(Tuple tuple) {
    if (tuple == null) {
      return null;
    }
    int index = 0;
    String zipCode = getString(tuple, index++);
    Long rideCount = getLong(tuple, index);
    return new ZipCodeReportEntry(zipCode, rideCount);
  }

  @Override
  public List<ZipCodeReportEntry> toDto(Iterable<Tuple> tuples) {
    List<ZipCodeReportEntry> resultList = new ArrayList<>();
    for (Tuple tuple : tuples) {
      resultList.add(toDto(tuple));
    }
    return resultList;
  }

  @Override
  public ZipCodeReportEntry convert(Tuple source) {
    return toDto(source);
  }
}
