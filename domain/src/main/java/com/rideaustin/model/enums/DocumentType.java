package com.rideaustin.model.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentType {

  LICENSE(false, true, "Driving license", Paths.DRIVER_LICENSES),
  INSURANCE(false, true, "Car insurance", Paths.DRIVER_INSURANCES),
  DRIVER_PHOTO(false, false, "Driver photo", Paths.DRIVER_PHOTOS),
  CAR_PHOTO_FRONT(false, false, "Car front photo", Paths.CAR_PHOTOS),
  CAR_PHOTO_BACK(false, false, "Car back photo", Paths.CAR_PHOTOS),
  CAR_PHOTO_INSIDE(false, false, "Car inside photo", Paths.CAR_PHOTOS),
  CAR_PHOTO_TRUNK(false, false, "Car trunk photo", Paths.CAR_PHOTOS),
  TNC_CARD(true, true, "TNC card", Paths.DOCUMENTS),
  CHAUFFEUR_LICENSE(true, true, "Chauffeur permit", Paths.DOCUMENTS),
  CAR_STICKER(false, true, "Car sticker", Paths.DOCUMENTS);

  public static final Set<DocumentType> DRIVER_DOCUMENTS = Collections.unmodifiableSet(EnumSet.of(LICENSE, DRIVER_PHOTO, TNC_CARD, CHAUFFEUR_LICENSE));
  public static final Set<DocumentType> CAR_DOCUMENTS = Collections.unmodifiableSet(EnumSet.of(INSURANCE, CAR_PHOTO_FRONT, CAR_PHOTO_INSIDE,
    CAR_PHOTO_TRUNK, CAR_PHOTO_BACK, CAR_STICKER));

  private final boolean citySpecific;
  private final boolean isPrivate;
  private final String defaultName;
  private final String folderName;

  private static final String FRONT = "FRONT";
  private static final String BACK = "BACK";
  private static final String INSIDE = "INSIDE";
  private static final String TRUNK = "TRUNK";
  private static final String PHOTO = "PHOTO";

  public static final String CAR_PHOTOS = "car-photos";

  public static final Set<DocumentType> CAR_PHOTO = Collections.unmodifiableSet(EnumSet.of(CAR_PHOTO_BACK, CAR_PHOTO_FRONT, CAR_PHOTO_INSIDE, CAR_PHOTO_TRUNK));

  static class Paths {
    static final String DOCUMENTS = "documents";
    static final String CAR_PHOTOS = DocumentType.CAR_PHOTOS;
    static final String DRIVER_PHOTOS = "driver-photos";
    static final String DRIVER_INSURANCES = "driver-insurances";
    static final String DRIVER_LICENSES = "driver-licenses";

    private Paths(){}
  }

  DocumentType(boolean citySpecific, boolean isPrivate, String defaultName, String folderName) {
    this.citySpecific = citySpecific;
    this.isPrivate = isPrivate;
    this.defaultName = defaultName;
    this.folderName = folderName;
  }

  public boolean isCitySpecific() {
    return citySpecific;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public String getDefaultName() {
    return defaultName;
  }

  public String getFolderName() {
    return folderName;
  }

  @Override
  @JsonValue
  public String toString() {
    if (CAR_PHOTO.contains(this)) {
      switch (this) {
        case CAR_PHOTO_BACK:
          return BACK;
        case CAR_PHOTO_FRONT:
          return FRONT;
        case CAR_PHOTO_INSIDE:
          return INSIDE;
        case CAR_PHOTO_TRUNK:
          return TRUNK;
        default:
          return "";
      }
    } else if (DRIVER_PHOTO.equals(this)) {
      return PHOTO;
    }
    return name();
  }
}
