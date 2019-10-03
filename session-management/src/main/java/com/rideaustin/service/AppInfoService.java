package com.rideaustin.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.AppInfo;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PlatformType;
import com.rideaustin.repo.dsl.AppInfoDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.PagingParams;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AppInfoService {

  private final AppInfoDslRepository appInfoRepository;

  @Nullable
  public AppInfo getAppInfo(@Nonnull AvatarType avatarType, @Nonnull PlatformType platformType) {
    return appInfoRepository.findByAvatarAndPlatform(avatarType, platformType);
  }

  public Page<AppInfo> listAppInfo(@Nullable AvatarType avatarType, @Nullable PlatformType platformType,
                                   Long cityId, @Nullable String search, PagingParams paging) {
    return appInfoRepository.listAppInfo(avatarType, platformType, cityId, search, paging);
  }

  @Nonnull
  public AppInfo updateAppInfo(long id, @Nonnull AppInfo info) throws NotFoundException {
    AppInfo appInfo = appInfoRepository.findOne(id);
    if (appInfo == null) {
      throw new NotFoundException("App info not found");
    }
    appInfo.copyFrom(info);
    return appInfoRepository.save(appInfo);
  }

  @Nonnull
  public AppInfo createAppInfo(@Nonnull AppInfo info) {
    AppInfo appInfo = new AppInfo();
    appInfo.copyFrom(info);
    return appInfoRepository.save(appInfo);
  }

  public void deleteAppInfo(long id) {
    appInfoRepository.delete(id);
  }
}
