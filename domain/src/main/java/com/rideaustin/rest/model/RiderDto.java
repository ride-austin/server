package com.rideaustin.rest.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Gender;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiderDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final AvatarType type;
  @ApiModelProperty(required = true)
  private final UserDto user;
  @ApiModelProperty(required = true)
  private final boolean cardExpired;
  @ApiModelProperty(required = true)
  private final boolean active;
  @JsonProperty("isDispatcher")
  @ApiModelProperty(required = true)
  private final boolean dispatcher;
  @ApiModelProperty(required = true)
  private final String firstname;
  @ApiModelProperty(required = true)
  private final String lastname;
  @ApiModelProperty(required = true)
  private final String fullName;
  @ApiModelProperty(required = true)
  private final String phoneNumber;
  @ApiModelProperty(required = true)
  private final String email;
  @ApiModelProperty(required = true)
  private final Double rating;
  @ApiModelProperty(required = true)
  private final CharityDto charity;

  public RiderDto(long id) {
    this.id = id;
    this.type = AvatarType.RIDER;
    this.user = new UserDto(1L, "", "", "", "", "", "", "", "", true, true, Gender.UNKNOWN, null, null, Collections.emptyList());
    this.cardExpired = false;
    this.active = true;
    this.dispatcher = false;
    this.firstname = "A";
    this.lastname = "B";
    this.fullName = "A B";
    this.phoneNumber = "";
    this.email = "";
    this.rating = 5.0;
    this.charity = null;
  }

  public RiderDto(@JsonProperty("id") long id, @JsonProperty("type") AvatarType type, @JsonProperty("user") UserDto user,
    @JsonProperty("cardExpired") boolean cardExpired, @JsonProperty("active") boolean active,
    @JsonProperty("isDispatcher") boolean dispatcher, @JsonProperty("firstname") String firstname,
    @JsonProperty("lastname") String lastname, @JsonProperty("fullName") String fullName,
    @JsonProperty("phoneNumber") String phoneNumber, @JsonProperty("email") String email,
    @JsonProperty("rating") Double rating, @JsonProperty("charity") CharityDto charity) {
    this.id = id;
    this.type = type;
    this.user = user;
    this.cardExpired = cardExpired;
    this.active = active;
    this.dispatcher = dispatcher;
    this.firstname = firstname;
    this.lastname = lastname;
    this.fullName = fullName;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.rating = rating;
    this.charity = charity;
  }

  @QueryProjection
  public RiderDto(long id, long userId, String photoUrl, String email, String facebookId, String firstname,
    String middleName, String lastname, String nickName, String phoneNumber, boolean active, boolean enabled, Gender gender,
    Address address, Date dateOfBirth, boolean cardExpired, boolean dispatcher, Double rating, long charityId, String charityName,
    String charityDescription, String charityImageUrl) {
    this.id = id;
    this.type = AvatarType.RIDER;
    this.user = new UserDto(userId, photoUrl, email, facebookId, firstname, middleName, lastname, nickName, phoneNumber,
      active, enabled, gender, address, dateOfBirth, Collections.singletonList(new UserDto.AvatarDto(id, AvatarType.RIDER, active)));
    this.cardExpired = cardExpired;
    this.active = active;
    this.dispatcher = dispatcher;
    this.firstname = firstname;
    this.lastname = lastname;
    this.fullName = String.format("%s %s", firstname, lastname);
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.rating = rating;
    this.charity = new CharityDto(charityId, charityName, charityDescription, charityImageUrl);
  }

  @Getter
  @Builder
  @ApiModel
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UserDto {

    @ApiModelProperty(required = true)
    private final long id;
    @ApiModelProperty(required = true)
    private final String photoUrl;
    @ApiModelProperty(required = true)
    private final String email;
    @ApiModelProperty
    private final String facebookId;
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
    private final boolean active;
    @ApiModelProperty(required = true)
    private final boolean enabled;
    @Setter
    @ApiModelProperty(required = true)
    private boolean deviceBlocked;
    @ApiModelProperty(required = true)
    private final String fullName;
    @ApiModelProperty(required = true)
    private final Gender gender;
    @ApiModelProperty
    private final Address address;
    @ApiModelProperty
    private final Date dateOfBirth;
    @ApiModelProperty(required = true)
    private final List<AvatarDto> avatars;

    public UserDto(@JsonProperty("id") long id, @JsonProperty("photoUrl") String photoUrl, @JsonProperty("email") String email,
      @JsonProperty("facebookId") String facebookId, @JsonProperty("firstname") String firstname,
      @JsonProperty("middleName") String middleName, @JsonProperty("lastname") String lastname,
      @JsonProperty("nickname") String nickName, @JsonProperty("phoneNumber") String phoneNumber,
      @JsonProperty("active") boolean active, @JsonProperty("enabled") boolean enabled,
      @JsonProperty("deviceBlocked") boolean deviceBlocked, @JsonProperty("fullName") String fullName,
      @JsonProperty("gender") Gender gender, @JsonProperty("address") Address address,
      @JsonProperty("dateOfBirth") Date dateOfBirth, @JsonProperty("avatars") List<AvatarDto> avatars) {
      this.id = id;
      this.photoUrl = photoUrl;
      this.email = email;
      this.facebookId = facebookId;
      this.firstname = firstname;
      this.middleName = middleName;
      this.lastname = lastname;
      this.nickName = nickName;
      this.phoneNumber = phoneNumber;
      this.active = active;
      this.enabled = enabled;
      this.deviceBlocked = deviceBlocked;
      this.fullName = fullName;
      this.gender = gender;
      this.address = address;
      this.dateOfBirth = dateOfBirth;
      this.avatars = avatars;
    }

    public UserDto(long id, String photoUrl, String email, String facebookId, String firstname, String middleName,
      String lastname, String nickName, String phoneNumber, boolean active, boolean enabled, Gender gender, Address address,
      Date dateOfBirth, List<AvatarDto> avatars) {
      this.id = id;
      this.photoUrl = photoUrl;
      this.email = email;
      this.facebookId = facebookId;
      this.firstname = firstname;
      this.middleName = middleName;
      this.lastname = lastname;
      this.nickName = nickName;
      this.phoneNumber = phoneNumber;
      this.active = active;
      this.enabled = enabled;
      this.gender = gender;
      this.address = address;
      this.dateOfBirth = dateOfBirth;
      this.avatars = avatars;
      this.fullName = String.format("%s %s", firstname, lastname);
    }

    @Getter
    @Builder
    @ApiModel
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvatarDto {
      @ApiModelProperty(required = true)
      private final long id;
      @ApiModelProperty(required = true)
      private final AvatarType type;
      @ApiModelProperty(required = true)
      private final boolean active;

      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public AvatarDto(@JsonProperty("id") long id, @JsonProperty("type") AvatarType type, @JsonProperty("active") boolean active) {
        this.id = id;
        this.type = type;
        this.active = active;
      }
    }
  }

  @Getter
  @Builder
  @ApiModel
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CharityDto {
    @ApiModelProperty(required = true)
    private final long id;
    @ApiModelProperty(required = true)
    private final String name;
    @ApiModelProperty(required = true)
    private final String description;
    @ApiModelProperty(required = true)
    private final String imageUrl;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CharityDto(@JsonProperty("id") long id, @JsonProperty("name") String name,
      @JsonProperty("description") String description, @JsonProperty("imageUrl") String imageUrl) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.imageUrl = imageUrl;
    }
  }
}
