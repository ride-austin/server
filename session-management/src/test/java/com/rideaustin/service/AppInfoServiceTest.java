package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.rideaustin.model.AppInfo;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PlatformType;
import com.rideaustin.repo.dsl.AppInfoDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.PagingParams;

public class AppInfoServiceTest {

  @Mock
  private AppInfoDslRepository appInfoRepository;

  private AppInfoService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new AppInfoService(appInfoRepository);
  }

  @Test
  public void getAppInfoDelegatesToRepo() {
    final AvatarType avatarType = AvatarType.RIDER;
    final PlatformType platformType = PlatformType.ANDROID;

    testedInstance.getAppInfo(avatarType, platformType);

    verify(appInfoRepository, times(1)).findByAvatarAndPlatform(eq(avatarType), eq(platformType));
  }

  @Test
  public void listAppInfoDelegatesToRepo() {
    final AvatarType avatarType = AvatarType.RIDER;
    final PlatformType platformType = PlatformType.ANDROID;
    final long cityId = 1L;
    final String search = "A";
    final PagingParams pagingParams = new PagingParams();

    testedInstance.listAppInfo(avatarType, platformType, cityId, search, pagingParams);

    verify(appInfoRepository, times(1)).listAppInfo(eq(avatarType), eq(platformType), eq(cityId),
      eq(search), eq(pagingParams));
  }

  @Test(expected = NotFoundException.class)
  public void updateAppInfoThrowsNotFoundException() throws NotFoundException {
    final long id = 1L;
    when(appInfoRepository.findOne(id)).thenReturn(null);

    testedInstance.updateAppInfo(id, new AppInfo());
  }

  @Test
  public void updateAppInfoCopiesAndSaves() throws NotFoundException {
    final long id = 1L;
    final AppInfo old = new AppInfo();
    when(appInfoRepository.findOne(id)).thenReturn(old);
    when(appInfoRepository.save(any(AppInfo.class))).thenAnswer((Answer<AppInfo>) invocation -> ((AppInfo) invocation.getArguments()[0]));

    final AppInfo updated = createAppInfo();

    final AppInfo result = testedInstance.updateAppInfo(id, updated);

    assertAppInfo(updated, result);
  }

  @Test
  public void createAppInfoSaves() {
    when(appInfoRepository.save(any(AppInfo.class))).thenAnswer((Answer<AppInfo>) invocation -> ((AppInfo) invocation.getArguments()[0]));

    final AppInfo newObject = createAppInfo();

    final AppInfo result = testedInstance.createAppInfo(newObject);

    assertEquals(newObject, result);
  }

  private void assertAppInfo(AppInfo expected, AppInfo actual) {
    assertEquals(expected.getAvatarType(), actual.getAvatarType());
    assertEquals(expected.getPlatformType(), actual.getPlatformType());
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getBuild(), actual.getBuild());
    assertEquals(expected.getMandatoryUpgrade(), actual.getMandatoryUpgrade());
    assertEquals(expected.getUserAgentHeader(), actual.getUserAgentHeader());
    assertEquals(expected.getDownloadUrl(), actual.getDownloadUrl());
  }

  private AppInfo createAppInfo() {
    final AppInfo updated = new AppInfo();
    updated.setAvatarType(AvatarType.RIDER);
    updated.setPlatformType(PlatformType.ANDROID);
    updated.setVersion("1");
    updated.setBuild(2);
    updated.setMandatoryUpgrade(false);
    updated.setUserAgentHeader("A");
    updated.setDownloadUrl("B");
    return updated;
  }
}