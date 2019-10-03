package com.rideaustin.rest.model;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverSpecialFlags;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.user.Gender;
import com.rideaustin.service.user.DriverTypeUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsoleDriverDto implements DriverOnboardingInfo {
  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private static final AvatarType type = AvatarType.DRIVER;
  @ApiModelProperty(required = true)
  private final CityApprovalStatus cityApprovalStatus;
  @ApiModelProperty(required = true)
  private final PayoneerStatus payoneerStatus;
  @ApiModelProperty(required = true)
  private final String directConnectId;
  @ApiModelProperty(required = true)
  private final String ssn;
  @ApiModelProperty(required = true)
  private final UserDto user;
  @ApiModelProperty(required = true)
  private final String licenseNumber;
  @ApiModelProperty(required = true)
  private final String licenseState;
  @ApiModelProperty(required = true)
  private final String activationNotes;
  @ApiModelProperty(required = true)
  private final DriverActivationStatus activationStatus;
  @ApiModelProperty(required = true)
  private final Set<DriverSpecialFlags> specialFlags;
  @ApiModelProperty(required = true)
  private final Set<String> grantedDriverTypes;
  @ApiModelProperty(required = true)
  private final double rating;
  @Setter
  @ApiModelProperty(required = true)
  private OnlineDriverStatus onlineStatus = OnlineDriverStatus.OFFLINE;
  @Setter
  @ApiModelProperty
  private boolean chauffeurLicense;
  @Setter
  @ApiModelProperty(required = true)
  private Collection<CarDto> cars;

  @QueryProjection
  public ConsoleDriverDto(long id, CityApprovalStatus cityApprovalStatus, PayoneerStatus payoneerStatus, String directConnectId,
    String ssn, String licenseNumber, String licenseState,
    String activationNotes, DriverActivationStatus activationStatus, String firstname, String middleName, String lastname,
    String nickName, String phoneNumber, Date dateOfBirth, Address address, String email, Gender gender, Integer specialFlags,
    Integer grantedDriverTypes, double rating) {
    this.id = id;
    this.cityApprovalStatus = cityApprovalStatus;
    this.payoneerStatus = payoneerStatus;
    this.directConnectId = directConnectId;
    this.ssn = ssn;
    this.licenseNumber = licenseNumber;
    this.licenseState = licenseState;
    this.activationNotes = activationNotes;
    this.activationStatus = activationStatus;
    this.specialFlags = DriverSpecialFlags.fromBitmask(specialFlags);
    this.grantedDriverTypes = DriverTypeUtils.fromBitMask(grantedDriverTypes);
    this.rating = rating;
    this.user = new UserDto(firstname, middleName, lastname, nickName, phoneNumber, dateOfBirth, address, email, gender,
      ImmutableSet.of(new UserDto.AvatarDto(id, AvatarType.DRIVER)));
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public ConsoleDriverDto(@JsonProperty("id") long id, @JsonProperty("cityApprovalStatus") CityApprovalStatus cityApprovalStatus,
    @JsonProperty("payoneerStatus") PayoneerStatus payoneerStatus, @JsonProperty("directConnectId") String directConnectId,
    @JsonProperty("ssn") String ssn, @JsonProperty("user") UserDto user, @JsonProperty("licenseNumber") String licenseNumber,
    @JsonProperty("licenseState") String licenseState, @JsonProperty("activationNotes") String activationNotes,
    @JsonProperty("activationStatus") DriverActivationStatus activationStatus,
    @JsonProperty("specialFlags") Set<DriverSpecialFlags> specialFlags,
    @JsonProperty("grantedDriverTypes") Set<String> grantedDriverTypes, @JsonProperty("rating") double rating) {
    this.id = id;
    this.cityApprovalStatus = cityApprovalStatus;
    this.payoneerStatus = payoneerStatus;
    this.directConnectId = directConnectId;
    this.ssn = ssn;
    this.user = user;
    this.licenseNumber = licenseNumber;
    this.licenseState = licenseState;
    this.activationNotes = activationNotes;
    this.activationStatus = activationStatus;
    this.specialFlags = specialFlags;
    this.grantedDriverTypes = grantedDriverTypes;
    this.rating = rating;
  }

  @Getter
  @ApiModel
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UserDto {
    @ApiModelProperty(required = true)
    private final String firstname;
    @ApiModelProperty
    private final String middleName;
    @ApiModelProperty(required = true)
    private final String lastname;
    @ApiModelProperty
    private final String nickName;
    @ApiModelProperty(required = true)
    private final String phoneNumber;
    @ApiModelProperty(required = true)
    private final Date dateOfBirth;
    @ApiModelProperty(required = true)
    private final Address address;
    @ApiModelProperty(required = true)
    private final String email;
    @ApiModelProperty(required = true)
    private final Gender gender;
    @ApiModelProperty(required = true)
    private final Set<AvatarDto> avatars;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public UserDto(@JsonProperty("firstname") String firstname, @JsonProperty("middleName") String middleName,
      @JsonProperty("lastname") String lastname, @JsonProperty("nickName") String nickName,
      @JsonProperty("phoneNumber") String phoneNumber, @JsonProperty("dateOfBirth") Date dateOfBirth,
      @JsonProperty("address") Address address, @JsonProperty("email") String email,
      @JsonProperty("gender") Gender gender, @JsonProperty("avatars") Set<AvatarDto> avatars) {
      this.firstname = firstname;
      this.middleName = middleName;
      this.lastname = lastname;
      this.nickName = nickName;
      this.phoneNumber = phoneNumber;
      this.dateOfBirth = dateOfBirth;
      this.address = address;
      this.email = email;
      this.gender = gender;
      this.avatars = avatars;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AvatarDto {
      @ApiModelProperty(required = true)
      private final long id;
      @ApiModelProperty(required = true)
      private final AvatarType type;

      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public AvatarDto(@JsonProperty("id") long id, @JsonProperty("type") AvatarType type) {
        this.id = id;
        this.type = type;
      }
    }
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  public String getFullName() {
    return String.format("%s %s", user.firstname, user.lastname);
  }

  public enum OnlineDriverStatus {
    ONLINE,
    OFFLINE,
    RIDING,
    STUCK
  }

}
