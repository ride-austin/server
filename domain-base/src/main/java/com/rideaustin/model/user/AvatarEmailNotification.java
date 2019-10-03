package com.rideaustin.model.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.rideaustin.model.enums.AvatarEmailNotificationType;

@Embeddable
public class AvatarEmailNotification {

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type")
  private AvatarEmailNotificationType type;

  @Column(name = "notification_date")
  private Date date;

  public AvatarEmailNotificationType getType() {
    return type;
  }

  public void setType(AvatarEmailNotificationType type) {
    this.type = type;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
