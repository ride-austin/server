package com.rideaustin.rest.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DriverSpecialFlags;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class MobileDriverDriverDto extends MobileDriverDto {

  @ApiModelProperty(required = true)
  private List<Car> cars;
  @ApiModelProperty(required = true, example = "1")
  private final long cityId;
  @ApiModelProperty(required = true)
  private final String directConnectId;
  @ApiModelProperty
  @Setter
  private boolean chauffeurPermit;
  @ApiModelProperty(required = true)
  private final boolean isDeaf;

  @QueryProjection
  public MobileDriverDriverDto(long id, double rating, Integer grantedDriverTypesBitmask, long userId, String email,
    String firstname, String lastname, String nickName, String phoneNumber, long cityId, String directConnectId,
    boolean active, Integer specialFlags) {
    super(id, rating, grantedDriverTypesBitmask, userId, email, firstname, lastname, nickName, phoneNumber, active);
    this.cityId = cityId;
    this.directConnectId = directConnectId;
    this.isDeaf = DriverSpecialFlags.fromBitmask(specialFlags).contains(DriverSpecialFlags.DEAF);
  }

  @Getter
  @ApiModel
  public static class Car extends MobileCar {
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Integer carCategoriesBitmask;
    @Setter
    @ApiModelProperty(required = true)
    private Set<String> carCategories;
    @ApiModelProperty(required = true)
    private boolean selected;
    @Setter
    @ApiModelProperty
    private String insurancePictureUrl;
    @ApiModelProperty(required = true)
    private CarInspectionStatus inspectionStatus;
    @ApiModelProperty(required = true)
    private boolean removed;
    @Setter
    @ApiModelProperty
    private Date insuranceExpiryDate;
    @ApiModelProperty
    private String inspectionNotes;

    @QueryProjection
    public Car(long id, String color, String license, String make, String model, String year, Integer carCategoriesBitmask,
      boolean selected, CarInspectionStatus inspectionStatus, boolean removed, String inspectionNotes) {
      super(id, color, license, make, model, year);
      this.selected = selected;
      this.inspectionStatus = inspectionStatus;
      this.removed = removed;
      this.inspectionNotes = inspectionNotes;
      this.carCategoriesBitmask = carCategoriesBitmask;
    }
  }

  @Override
  public List<Car> getCars() {
    return cars;
  }

  @Override
  public void setCars(List<? extends MobileCar> cars) {
    this.cars = new ArrayList<>();
    for (MobileCar car : cars) {
      this.cars.add((Car) car);
    }
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  public boolean isAgreedToLegalTerms(){
    return true;
  }
}
