package com.rideaustin.utils;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.BeanUtils;

import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.DriverOnboardingInfo;

public class DriverUtils {

  private static final String SSN_MASK_CHAR = "X";
  private static final int SSN_MASK_LENGTH_REMAIN = 4;

  private DriverUtils(){}

  @Nonnull
  public static String maskSsn(@Nonnull String ssn) {
    int length = ssn.length();
    if (length < SSN_MASK_LENGTH_REMAIN) {
      return ssn;
    }
    int xLength = length - SSN_MASK_LENGTH_REMAIN;
    return StringUtils.repeat(SSN_MASK_CHAR, xLength) + ssn.substring(xLength, length);
  }

  @Nullable
  public static String fixNameCase(@Nullable String name) {
    if (StringUtils.isEmpty(name)) {
      return null;
    }
    if (StringUtils.isAllUpperCase(name) || Character.isLowerCase(name.charAt(0))) {
      return WordUtils.capitalizeFully(name);
    }
    return name;
  }

  public static DriverOnboardingInfo createCopy(Driver driver) {
    Driver result = new Driver();
    BeanUtils.copyProperties(driver, result);
    return result;
  }

  public static List<Car> getActiveCars(Driver driver) {
    return driver.getCars().stream().filter(car -> !car.isRemoved()).collect(Collectors.toList());
  }

}
