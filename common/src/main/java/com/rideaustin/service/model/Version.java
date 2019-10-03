package com.rideaustin.service.model;

import java.util.Objects;

import lombok.Getter;

@Getter
public class Version implements Comparable<Version> {
  private final int major;
  private final int minor;
  private final int hotfix;

  public Version(String version) {
    String[] split = version.split("\\.");
    if (split.length != 3) {
      major = minor = hotfix = 0;
    } else {
      major = Integer.parseInt(split[0]);
      minor = Integer.parseInt(split[1]);
      hotfix = Integer.parseInt(split[2]);
    }
  }

  @Override
  public int compareTo(Version other) {
    boolean majorEqual = this.major == other.major;
    if (majorEqual) {
      boolean minorEqual = this.minor == other.minor;
      if (minorEqual) {
        return Integer.compare(this.hotfix, other.hotfix);
      } else {
        return Integer.compare(this.minor, other.minor);
      }
    } else {
      return Integer.compare(this.major, other.major);
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
    Version version = (Version) o;
    return major == version.major &&
      minor == version.minor &&
      hotfix == version.hotfix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, hotfix);
  }

  @Override
  public String toString() {
    return String.format("Version{%d.%d.%d}", major, minor, hotfix);
  }
}
