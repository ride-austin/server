package com.rideaustin.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class ClientAppVersionFactoryTest {

  private HttpServletRequest request;
  private ClientAppVersionFactory factory = new ClientAppVersionFactory();

  @Before
  public void setup() {
    request = mock(HttpServletRequest.class);
  }

  @Test
  public void testClientVersion1() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("RideAustin_iOS_1.7.0");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.RIDER));
    assertThat(version.getAgentBuild(), is(0));
    assertThat(version.getRawPlatform(), is(ClientPlatform.IOS));
  }

  @Test
  public void testClientVersion2() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("RideAustin_iOS_2.5.0 (215)");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.RIDER));
    assertThat(version.getAgentBuild(), is(215));
    assertThat(version.getRawPlatform(), is(ClientPlatform.IOS));
  }

  @Test
  public void testClientVersion3() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("RideAustinRider_Android_2.5.1-build#Android Studio (30)");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.RIDER));
    assertThat(version.getAgentBuild(), is(30));
    assertThat(version.getRawPlatform(), is(ClientPlatform.ANDROID));
  }

  @Test
  public void testClientVersion4() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("RideAustinRider_Android_1.1.3 (26)");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.RIDER));
    assertThat(version.getAgentBuild(), is(26));
    assertThat(version.getRawPlatform(), is(ClientPlatform.ANDROID));
  }

  @Test
  public void testClientVersion5() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("RideAustinDriver_iOS_2.4.0 (219)");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.DRIVER));
    assertThat(version.getAgentBuild(), is(219));
    assertThat(version.getRawPlatform(), is(ClientPlatform.IOS));
  }

  @Test
  public void testClientVersion6() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("RideAustinDriver_Android_1.0.918 (9)");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.DRIVER));
    assertThat(version.getAgentBuild(), is(9));
    assertThat(version.getRawPlatform(), is(ClientPlatform.ANDROID));
  }

  @Test
  public void testClientVersion7() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("okhttp/2.3.0");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.UNKNOWN));
    assertThat(version.getAgentBuild(), is(0));
    assertThat(version.getRawPlatform(), is(ClientPlatform.OTHER));
  }

  @Test
  public void testClientVersion8() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.UNKNOWN));
    assertThat(version.getAgentBuild(), is(0));
    assertThat(version.getRawPlatform(), is(ClientPlatform.OTHER));
  }

  @Test
  public void testClientVersion9() {
    when(request.getHeader(ClientAppVersionFactory.USER_AGENT)).thenReturn("Dalvik/1.6.0 (Linux; U; Android 4.2.2; SCH-I545 Build/JDQ39E)");
    ClientAppVersion version = factory.createClientAppVersion(request);
    assertThat(version.getClientType(), is(ClientType.RIDER));
    assertThat(version.getAgentBuild(), is(0));
    assertThat(version.getRawPlatform(), is(ClientPlatform.ANDROID));
  }

}