package com.rideaustin.model.user;

import java.beans.Transient;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rideaustin.Constants;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.ForbiddenException;

@Entity
@Table(name = "avatars")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class Avatar extends BaseEntity {

  public static class Info {
    private Long id;
    private AvatarType type;
    private boolean active;

    public Info() {
    }

    public Info(Avatar a) {
      this.id = a.getId();
      this.type = a.getType();
      this.active = a.isActive();
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public AvatarType getType() {
      return type;
    }

    public void setType(AvatarType type) {
      this.type = type;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }
  }

  @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  @NotNull(message = "User is required")
  @Valid
  protected User user;

  @Column(name = "active")
  private boolean active;

  @javax.persistence.Transient
  private Date lastLoginDate;

  @JsonIgnore
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "avatar_email_notifications", joinColumns = @JoinColumn(name = "avatar_id", nullable = false))
  private Set<AvatarEmailNotification> notifications = new HashSet<>();

  protected Avatar() {
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<AvatarEmailNotification> getNotifications() {
    return notifications;
  }

  public void setNotifications(Set<AvatarEmailNotification> notifications) {
    this.notifications = notifications;
  }

  @Transient
  @JsonProperty
  public String getEmail() {
    return user.getEmail();
  }

  @Transient
  @JsonProperty
  public Gender getGender() {
    return user.getGender();
  }

  @Transient
  @JsonProperty
  public String getFacebookId() {
    return user.getFacebookId();
  }

  @Transient
  @JsonProperty
  public String getFirstname() {
    return user.getFirstname();
  }

  @Transient
  @JsonProperty
  public String getFullName() {
    return user.getFullName();
  }

  @Transient
  @JsonProperty
  public String getLastname() {
    return user.getLastname();
  }

  @Transient
  @JsonProperty
  public String getPhoneNumber() {
    return user.getPhoneNumber();
  }

  @Transient
  @JsonProperty
  public Date getLastLoginDate() {
    return lastLoginDate;
  }

  public void setLastLoginDate(Date lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
  }

  public Long getCityId() {
    return Constants.DEFAULT_CITY_ID;
  }

  @JsonProperty
  @Transient
  public abstract AvatarType getType();

  public void checkAccess(User user) throws ForbiddenException {
    if (user.isAdmin()) {
      return;
    }

    if (user.getId() != getUser().getId()) {
      throw new ForbiddenException();
    }
  }

  /**
   * Update the Avatar object by the user himself
   */
  public void updateByUser(Avatar avatar) {
    if (avatar.getUser() != null) {
      getUser().updateByUser(avatar.getUser());
    }
  }

  public void updateByAdmin(Avatar avatar) {
    if (avatar.getUser() != null) {
      getUser().updateByAdmin(avatar.getUser());
      setActive(avatar.isActive());
    }
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
