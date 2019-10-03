package com.rideaustin.rest.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.enums.AvatarType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MobileDriverDto {

  @ApiModelProperty(required = true)
  private long id;
  @ApiModelProperty(required = true)
  private User user;
  @ApiModelProperty(required = true)
  private List<? extends MobileCar> cars;
  @ApiModelProperty(required = true)
  private Double rating;
  @ApiModelProperty(required = true)
  private String email;
  @ApiModelProperty(required = true)
  private String phoneNumber;
  @ApiModelProperty(required = true)
  private String photoUrl;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private Integer grantedDriverTypesBitmask;
  @Setter
  @ApiModelProperty(required = true)
  private Set<String> grantedDriverTypes;
  @ApiModelProperty(required = true)
  private boolean active;
  @ApiModelProperty(required = true)
  private AvatarType type = AvatarType.DRIVER;

  public MobileDriverDto(long id, Double rating, Integer grantedDriverTypesBitmask, long userId, String email,
    String firstname, String lastname, String nickName, String phoneNumber, boolean active) {
    this.id = id;
    this.rating = rating;
    this.grantedDriverTypesBitmask = grantedDriverTypesBitmask;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.user = new User(userId, id, active, email, firstname, lastname, nickName, phoneNumber);
    this.active = active;
  }

  @JsonProperty
  public String getFirstname() {
    return user.firstname;
  }

  @JsonProperty
  public String getLastname() {
    return user.lastname;
  }

  @JsonProperty
  public String getFullname() {
    return user.getFullname();
  }

  public String getNickName() {
    return user.nickName;
  }


  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
    this.user.setPhotoUrl(photoUrl);
  }

  public void setCars(List<? extends MobileCar> cars) {
    this.cars = cars;
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  public boolean isActive() {
    return true;
  }

  @Getter
  @ApiModel
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class User {
    @ApiModelProperty(required = true)
    private final long id;
    @ApiModelProperty(required = true)
    private final Set<Avatar> avatars;
    @ApiModelProperty(required = true)
    private final String email;
    @ApiModelProperty(required = true)
    private final String firstname;
    @ApiModelProperty(required = true)
    private final String lastname;
    @ApiModelProperty(required = true)
    private final String nickName;
    @ApiModelProperty(required = true)
    private final String phoneNumber;
    @Setter
    @ApiModelProperty
    private String photoUrl;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public User(@JsonProperty("id") long id, @JsonProperty("avatars") Set<Avatar> avatars, @JsonProperty("email") String email,
      @JsonProperty("firstname") String firstname, @JsonProperty("lastname") String lastname,
      @JsonProperty("nickName") String nickName, @JsonProperty("phoneNumber") String phoneNumber) {
      this.id = id;
      this.avatars = avatars;
      this.email = email;
      this.firstname = firstname;
      this.phoneNumber = phoneNumber;
      this.lastname = lastname;
      this.nickName = nickName;
    }

    public User(long id, long driverId, boolean active, String email, String firstname, String lastname, String nickName,
      String phoneNumber) {
      this(id, ImmutableSet.of(new Avatar(driverId, AvatarType.DRIVER, active)), email, firstname, lastname, nickName,
        phoneNumber);
    }

    @JsonProperty
    public String getFullname() {
      return String.format("%s %s", firstname, lastname);
    }

    @Getter
    @ApiModel
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Avatar {
      @ApiModelProperty(required = true)
      private final long id;
      @ApiModelProperty(required = true)
      private final AvatarType type;
      @ApiModelProperty(required = true)
      private final boolean active;

      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public Avatar(@JsonProperty("id") long id, @JsonProperty("type") AvatarType type, @JsonProperty("active") boolean active) {
        this.id = id;
        this.type = type;
        this.active = active;
      }
    }
  }

  @Getter
  @ApiModel
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class MobileCar {
    @ApiModelProperty(required = true)
    private final long id;
    @Setter
    @ApiModelProperty(required = true)
    private String photoUrl;
    @ApiModelProperty(required = true)
    private final String color;
    @ApiModelProperty(required = true)
    private final String license;
    @ApiModelProperty(required = true)
    private final String make;
    @ApiModelProperty(required = true)
    private final String model;
    @ApiModelProperty(required = true)
    private final String year;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MobileCar(@JsonProperty("id") long id, @JsonProperty("color") String color, @JsonProperty("license") String license,
      @JsonProperty("make") String make, @JsonProperty("model") String model, @JsonProperty("year") String year) {
      this.id = id;
      this.color = color;
      this.license = license;
      this.make = make;
      this.model = model;
      this.year = year;
    }
  }
}
