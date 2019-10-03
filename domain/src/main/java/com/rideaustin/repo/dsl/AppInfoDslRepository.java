package com.rideaustin.repo.dsl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.AppInfo;
import com.rideaustin.model.QAppInfo;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PlatformType;
import com.rideaustin.rest.model.PagingParams;

@Repository
public class AppInfoDslRepository extends AbstractDslRepository {

  private static final QAppInfo qAppInfo = QAppInfo.appInfo;

  public void delete(long id) {
    delete(findOne(id));
  }

  public AppInfo findOne(long id) {
    return get(id, AppInfo.class);
  }

  public AppInfo findByAvatarAndPlatform(@Nonnull AvatarType avatarType, @Nonnull PlatformType platformType) {
    List<AppInfo> list = buildQuery(qAppInfo)
      .where(qAppInfo.avatarType.eq(avatarType).and(qAppInfo.platformType.eq(platformType)))
      .orderBy(qAppInfo.createdDate.desc())
      .limit(1)
      .fetch();
    return list.isEmpty() ? null : list.get(0);
  }

  public Page<AppInfo> listAppInfo(@Nullable AvatarType avatarType, @Nullable PlatformType platformType, Long cityId,
    @Nullable String search, PagingParams paging) {
    BooleanBuilder where = new BooleanBuilder();
    if (avatarType != null) {
      where.and(qAppInfo.avatarType.eq(avatarType));
    }
    if (platformType != null) {
      where.and(qAppInfo.platformType.eq(platformType));
    }

    if (StringUtils.isNotBlank(search)) {
      where.and(qAppInfo.version.containsIgnoreCase(search)
        .or(qAppInfo.userAgentHeader.containsIgnoreCase(search)));
    }

    where.and(qAppInfo.cityId.eq(cityId));

    JPAQuery<AppInfo> query = buildQuery(qAppInfo).where(where);
    long total = query.fetchCount();

    if (paging != null) {
      query = appendPagingParams(query, paging, qAppInfo);
    }

    return getPage(paging, query.fetch(), total);
  }
}
