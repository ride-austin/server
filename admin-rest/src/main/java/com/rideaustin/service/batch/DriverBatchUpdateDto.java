package com.rideaustin.service.batch;

import java.util.Date;
import java.util.Set;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.user.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validate(with = {EmailBatchValidator.class, ActivationBatchValidator.class})
public class DriverBatchUpdateDto {

  private Long id;
  private String firstName;
  private String middleName;
  private String lastName;
  private String phoneNumber;
  @Validate(with = EmailBatchValidator.class)
  private String email;
  private String ssn;
  @Validate
  private Date dateOfBirth;
  private Boolean active;
  private Boolean enabled;
  @Validate(with = DriverTypesBatchValidator.class)
  private Set<String> driverTypes;
  @Validate
  private Gender gender;
  private Double rating;
  @Validate
  private PayoneerStatus payoneerStatus;
  @Validate
  private CityApprovalStatus cityApprovalStatus;
  @Validate
  private DriverActivationStatus activationStatus;
  private String activationNotes;
  @Validate
  private DocumentStatus driverLicenseStatus;
  @Validate
  private Date licenseExpiryDate;
  @Validate
  private DocumentStatus profilePhotosStatus;
  private String licenseNumber;
  private String licenseState;
  @Validate(with = CityBatchValidator.class)
  private Constants.City city;


}
