package com.rideaustin.model.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.rideaustin.model.enums.CityEmailType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "driver_email_reminders")
public class DriverEmailReminder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "subject")
  private String subject;

  @Column(name = "email_template")
  private String emailTemplate;

  @Column(name = "store_content")
  private boolean storeContent;

  @Column(name = "email_type")
  @Enumerated(EnumType.STRING)
  private CityEmailType emailType;

  @Column(name = "city_id")
  private long cityId;

}
