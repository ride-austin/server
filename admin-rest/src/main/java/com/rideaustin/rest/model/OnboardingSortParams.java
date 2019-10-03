package com.rideaustin.rest.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;

public enum OnboardingSortParams {
  CAR_INSPECTION_STATUS("carInspectionStatus") {
    @Override
    public Function<SimpleDriverDto, CarInspectionStatus> sortField() {
      return dto -> {
        List<SimpleCarDto> cars = dto.getCars();
        return cars.isEmpty() ? CarInspectionStatus.NOT_INSPECTED : cars.get(0).getInspectionStatus();
      };
    }
  },
  DRIVER_LICENSE_STATUS("driverLicenseStatus") {
    @Override
    public Function<SimpleDriverDto, DocumentStatus> sortField() {
      return SimpleDriverDto::getDriverLicenseStatus;
    }
  },
  INSURANCE_STATUS("insuranceStatus") {
    @Override
    public Function<SimpleDriverDto, DocumentStatus> sortField() {
      return dto -> {
        List<SimpleCarDto> cars = dto.getCars();
        return cars.isEmpty() ? DocumentStatus.PENDING : cars.get(0).getInsuranceStatus();
      };
    }
  },
  CAR_PHOTOS_STATUS("carPhotosStatus") {
    @Override
    public Function<SimpleDriverDto, DocumentStatus> sortField() {
      return dto -> {
        List<SimpleCarDto> cars = dto.getCars();
        return cars.isEmpty() ? DocumentStatus.PENDING : cars.get(0).getCarPhotosStatus().get(DocumentType.CAR_PHOTO_FRONT);
      };
    }
  },
  PROFILE_PHOTOS_STATUS("profilePhotosStatus") {
    @Override
    public Function<SimpleDriverDto, DocumentStatus> sortField() {
      return SimpleDriverDto::getProfilePhotosStatus;
    }
  },
  INSPECTION_STICKER_STATUS("inspectionStickerStatus") {
    @Override
    public Function<SimpleDriverDto, DocumentStatus> sortField() {
      return dto -> {
        List<SimpleCarDto> cars = dto.getCars();
        return cars.isEmpty() ? DocumentStatus.PENDING : cars.get(0).getInspectionStickerStatus();
      };
    }
  };

  private final String key;

  OnboardingSortParams(String key) {
    this.key = key;
  }

  public static OnboardingSortParams from(List<String> value) {
    String sort = value.size() == 1 ? value.get(0) : "id";
    return Arrays.stream(values()).filter(v -> v.key.equals(sort)).findFirst().orElse(null);
  }

  public abstract <T> Function<SimpleDriverDto, Comparable<T>> sortField();
}
