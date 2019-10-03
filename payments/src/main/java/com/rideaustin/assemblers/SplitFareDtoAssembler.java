package com.rideaustin.assemblers;

import java.text.SimpleDateFormat;

import org.springframework.stereotype.Component;

import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.rest.model.SplitFareDto;

@Component
public class SplitFareDtoAssembler implements SingleSideAssembler<FarePayment, SplitFareDto> {

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  public SplitFareDto toDto(FarePayment source) {
    SplitFareDto.SplitFareDtoBuilder builder = SplitFareDto.builder()
      .id(source.getId())
      .rideId(source.getRide().getId())
      .riderId(source.getRider().getId())
      .riderFullName(source.getRider().getFullName())
      .status(source.getSplitStatus())
      .riderPhoto(source.getRider().getUser().getPhotoUrl())
      .createdDate(sdf.format(source.getCreatedDate()));
    if (source.getUpdatedDate() != null) {
      builder.updatedDate(sdf.format(source.getUpdatedDate()));
    }
    return builder.build();
  }
}
