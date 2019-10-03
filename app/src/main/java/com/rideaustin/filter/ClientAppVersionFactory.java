package com.rideaustin.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class ClientAppVersionFactory {

  static final String USER_AGENT = "User-Agent";
  static final String USER_PLATFORM = "User-Platform";
  static final String USER_DEVICE = "User-Device";
  static final String USER_DEVICE_ID = "User-Device-Id";
  static final String USER_DEVICE_OTHER = "User-Device-Other";
  static final String ANDROID = "android";
  static final String IOS = "ios";
  private final Pattern buildVersionPattern = Pattern.compile("^.*\\((\\d{1,4})\\)$");

  public ClientAppVersion createClientAppVersion(HttpServletRequest request) {
    ClientAppVersion clientAppVersion = new ClientAppVersion();
    clientAppVersion.setUserAgent(request.getHeader(USER_AGENT));
    clientAppVersion.setUserPlatform(request.getHeader(USER_PLATFORM));
    clientAppVersion.setUserDevice(request.getHeader(USER_DEVICE));
    clientAppVersion.setUserDeviceId(request.getHeader(USER_DEVICE_ID));
    clientAppVersion.setUserDeviceOther(request.getHeader(USER_DEVICE_OTHER));

    if (clientAppVersion.getUserAgent() != null) {
      String userAgentLowerCase = clientAppVersion.getUserAgent().toLowerCase();
      clientAppVersion.setClientAgentCity(deriveClientAgentCity(userAgentLowerCase));

      if (userAgentLowerCase.contains(ANDROID)) {
        clientAppVersion.setRawPlatform(ClientPlatform.ANDROID);
        clientAppVersion.setAgentBuild(extractBuildVersion(userAgentLowerCase));
        clientAppVersion.setClientType(deriveRawPlatform(userAgentLowerCase));
      } else if (userAgentLowerCase.contains(IOS)) {
        clientAppVersion.setRawPlatform(ClientPlatform.IOS);
        clientAppVersion.setAgentBuild(extractBuildVersion(userAgentLowerCase));
        clientAppVersion.setClientType(deriveRawPlatform(userAgentLowerCase));
      } else {
        clientAppVersion.setRawPlatform(ClientPlatform.OTHER);
        clientAppVersion.setAgentBuild(0);
        clientAppVersion.setClientType(ClientType.UNKNOWN);
      }
    } else {
      clientAppVersion.setClientType(ClientType.UNKNOWN);
      clientAppVersion.setAgentBuild(0);
      clientAppVersion.setRawPlatform(ClientPlatform.OTHER);
    }

    return clientAppVersion;
  }

  private ClientAgentCity deriveClientAgentCity(String userAgentLowerCase) {
    if (userAgentLowerCase.contains("austin")) {
      return ClientAgentCity.AUSTIN;
    } else if (userAgentLowerCase.contains("houston")) {
      return ClientAgentCity.HOUSTON;
    } else {
      return ClientAgentCity.UNKNOWN;
    }
  }

  private ClientType deriveRawPlatform(String userAgentLowerCase) {
    if (userAgentLowerCase.contains("driver")) {
      return ClientType.DRIVER;
    } else {
      return ClientType.RIDER;
    }
  }

  private Integer extractBuildVersion(String userAgentLowerCase) {
    try {
      Matcher m = buildVersionPattern.matcher(userAgentLowerCase);
      if (m.matches()) {
        return Integer.parseInt(m.group(m.groupCount()));
      }
    } catch (NumberFormatException e) {
      //if we don't match build version we treat as the earliest build version
    }
    return 0;
  }
}
