package com.rideaustin.service.config;

public enum SurgeMode {

  FULL_AUTO,
  LIMITED_AUTO,
  MANUAL;

  public static SurgeMode fromKey(String key){
    for(SurgeMode v : values()){
      if( v.name().equals(key)){
        return v;
      }
    }
    return null;
  }

}
