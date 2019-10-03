package com.rideaustin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.SessionClosingReason;
import com.rideaustin.model.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sessions")
@RedisHash(timeToLive = 3600)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends BaseEntity {

  @Id
  @Transient
  private String redisKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "api_client_app_type")
  private ApiClientAppType apiClientAppType;

  @Column
  private String version;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "user_platform")
  private String userPlatform;

  @Column(name = "user_device")
  private String userDevice;

  @Column(name = "user_device_id")
  private String userDeviceId;

  @Column(name = "user_device_other")
  private String userDeviceOther;

  @Column(name = "token_uuid")
  private String tokenUuid;

  @Column(name = "auth_token", unique = true)
  private String authToken;

  @Column(name = "expires_on")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expiresOn;

  @Column(name = "session_closing_reason")
  private SessionClosingReason sessionClosingReason;

  @Column
  private boolean deleted;

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


