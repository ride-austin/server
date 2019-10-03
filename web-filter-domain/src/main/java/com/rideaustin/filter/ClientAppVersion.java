package com.rideaustin.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientAppVersion {

  private String userAgent;
  private String userPlatform;
  private String userDevice;
  private String userDeviceId;
  private String userDeviceOther;
  private ClientType clientType;
  private ClientAgentCity clientAgentCity;
  private ClientPlatform rawPlatform;
  private int agentBuild = 0;

}
