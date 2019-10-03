package com.rideaustin.model.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.Address;
import com.rideaustin.model.BaseEntityPhoto;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Avatar.Info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseEntityPhoto implements UserDetails {

  @Column(name = "avatar_types_bitmask")
  @JsonIgnore
  private int avatarTypesBitmask;

  @Column(nullable = false, unique = true)
  @NotEmpty(message = "Email is required")
  @Email(message = "Must be a valid email")
  private String email;

  @Column(name = "facebook_id")
  private String facebookId;

  @Column(name = "first_name", nullable = false)
  @NotEmpty(message = "First name is required")
  private String firstname;

  @Column(name = "middle_name")
  private String middleName;

  @Column(name = "last_name", nullable = false)
  @NotEmpty(message = "Last name is required")
  private String lastname;

  @Column(name = "nick_name")
  private String nickName;

  @Column(name = "phone_number", nullable = false)
  @NotEmpty(message = "Phone number is required")
  private String phoneNumber;

  @Column(name = "phone_number_verified")
  private boolean phoneNumberVerified = true;

  @Column(name = "email_verified")
  private boolean emailVerified = true;

  @Embedded
  private Address address = new Address();

  @Column(name = "date_of_birth")
  @Temporal(TemporalType.DATE)
  private Date dateOfBirth;

  @Column(name = "gender")
  @Enumerated(EnumType.STRING)
  private Gender gender = Gender.UNKNOWN;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
  @Fetch(value = FetchMode.SELECT)
  @JsonIgnore
  private List<Avatar> avatars = new ArrayList<>();

  @Column
  @JsonIgnore
  private String password;

  @Transient
  private String rawPassword;

  @Transient
  @JsonIgnore
  private Set<AvatarType> avatarTypes = new HashSet<>();

  @Column(name = "enabled")
  @JsonProperty("enabled")
  private Boolean userEnabled = false;

  @Transient
  @JsonIgnore
  public boolean isRider() {
    return avatarTypes.contains(AvatarType.RIDER);
  }

  @Transient
  @JsonIgnore
  public boolean isDriver() {
    return avatarTypes.contains(AvatarType.DRIVER);
  }

  @Transient
  @JsonIgnore
  public boolean isAdmin() {
    return avatarTypes.contains(AvatarType.ADMIN);
  }

  @Transient
  @JsonIgnore
  public boolean isApiClient() {
    return avatarTypes.contains(AvatarType.API_CLIENT);
  }

  @Transient
  @JsonProperty
  public String getFullName() {
    return this.firstname + " " + this.lastname;
  }

  @Override
  @PrePersist
  protected void onCreate() {
    email = email == null ? null : email.toLowerCase();
    super.onCreate();
  }

  @Override
  @PreUpdate
  protected void onUpdate() {
    email = email == null ? null : email.toLowerCase();
    super.onUpdate();
  }

  @PostLoad
  private void onLoad() {
    avatarTypes = new HashSet<>();
    avatarTypes.addAll(AvatarType.fromBitMask(avatarTypesBitmask));
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (avatarTypes != null) {
      if (avatarTypesBitmask != 0 && avatarTypes.isEmpty()) {
        onLoad();
      }
      return avatarTypes.stream()
        .map(AvatarType::roleName)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  @JsonProperty("avatars")
  public List<Avatar.Info> getAvatarsInfo() {
    if (avatars != null) {
      return avatars.stream()
        .map(Avatar.Info::new)
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  @Override
  @JsonIgnore
  public String getUsername() {
    return email;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return Boolean.TRUE.equals(userEnabled);
  }

  public void updateByAdmin(User user) {
    updateByUser(user);
    setUserEnabled(user.getUserEnabled());
    setDateOfBirth(user.getDateOfBirth());
    setAddress(user.getAddress());
    setEmail(user.getEmail());
    setFacebookId(user.getFacebookId());
    setGender(user.getGender());
  }

  /**
   * Update the user object by the user himself
   */
  public void updateByUser(User user) {
    if (!user.hasAvatar(AvatarType.DRIVER)) {
      setFirstname(user.getFirstname());
      setMiddleName(user.getMiddleName());
      setLastname(user.getLastname());
    }
    setNickName(user.getNickName());
    setPhoneNumber(user.getPhoneNumber());
  }

  public User deepCopy(User user) {
    User ret = new User();
    BeanUtils.copyProperties(this, ret, "avatars", "avatarTypesBitMask", "password", "address");
    boolean admin = user.isAdmin();
    boolean sameUser = user.getId() == getId();
    if (admin) {
      ret.setUserEnabled(getUserEnabled());
      ret.setPhoneNumberVerified(isPhoneNumberVerified());
    }
    if (admin || sameUser) {
      ret.setAddress(getAddress());
    }
    if (!sameUser && !admin) {
      ret.setLastname("");
    }
    return ret;
  }

  public Info avatarInfo(AvatarType avatarType) {
    for (Info info : getAvatarsInfo()) {
      if (info.getType().equals(avatarType)) {
        return info;
      }
    }
    return null;
  }

  public <T extends Avatar> T getAvatar(Class<T> clazz) {
    return getAvatars().stream()
      .filter(a -> a.getClass().isAssignableFrom(clazz))
      .findFirst()
      .map(clazz::cast)
      .orElse(null);
  }

  public boolean hasAvatar(AvatarType avatarType) {
    return (avatarTypesBitmask & avatarType.toBitMask()) != 0;
  }

  public void addAvatar(Avatar avatar) {
    this.avatarTypes.add(avatar.getType());
    this.avatars.add(avatar);
    this.avatarTypesBitmask |= avatar.getType().toBitMask();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
