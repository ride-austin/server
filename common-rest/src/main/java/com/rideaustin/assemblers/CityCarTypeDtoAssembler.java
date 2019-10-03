package com.rideaustin.assemblers;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.rest.model.CityCarTypeDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CityCarTypeDtoAssembler implements SingleSideAssembler<CityCarType, CityCarTypeDto> {

  private final DecimalFormat format;
  private final ObjectMapper mapper;

  @Inject
  public CityCarTypeDtoAssembler(ObjectMapper mapper) {
    this.mapper = mapper;
    this.format = new DecimalFormat();
    this.format.setMaximumFractionDigits(2);
    this.format.setMinimumFractionDigits(2);
  }

  @Override
  public CityCarTypeDto toDto(CityCarType cityCarType) {
    String configuration = null;
    try {
      configuration = mapper.writeValueAsString(cityCarType.getConfigurationObject(mapper));
    }
    catch (IOException e) {
      log.error("Failed to write config", e);
    }
    return CityCarTypeDto.builder()
      .carCategory(cityCarType.getCarType().getCarCategory())
      .title(cityCarType.getCarType().getTitle())
      .description(cityCarType.getCarType().getDescription())
      .iconUrl(cityCarType.getCarType().getIconUrl())
      .plainIconUrl(cityCarType.getCarType().getPlainIconUrl())
      .mapIconUrl(cityCarType.getCarType().getMapIconUrl())
      .fullIconUrl(cityCarType.getCarType().getFullIconUrl())
      .selectedFemaleIconUrl(cityCarType.getCarType().getSelectedFemaleIconUrl())
      .selectedIconUrl(cityCarType.getCarType().getSelectedIconUrl())
      .unselectedIconUrl(cityCarType.getCarType().getUnselectedIconUrl())
      .configuration(configuration)
      .maxPersons(cityCarType.getCarType().getMaxPersons())
      .order(cityCarType.getCarType().getOrder())
      .active(cityCarType.getCarType().getActive())
      .cityId(cityCarType.getCityId())
      .minimumFare(cityCarType.getMinimumFare())
      .baseFare(cityCarType.getBaseFare())
      .bookingFee(cityCarType.getBookingFee())
      .raFixedFee(cityCarType.getFixedRAFee())
      .ratePerMile(cityCarType.getRatePerMile())
      .ratePerMinute(cityCarType.getRatePerMinute())
      .cancellationFee(cityCarType.getCancellationFee())
      .tncFeeRate(cityCarType.getCityFeeRate().multiply(BigDecimal.valueOf(100L)))
      .processingFeeRate(cityCarType.getProcessingFeeRate())
      .processingFee(format.format(cityCarType.getProcessingFeeRate()))
      .processingFeeText(cityCarType.getProcessingFeeText())
      .build();
  }
}
