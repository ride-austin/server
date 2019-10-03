package com.rideaustin.application.cache;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.ExternalEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.ServerError;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/cache")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CacheEndpoint {

  private final CacheService cacheService;

  @ExternalEndpoint
  @PostMapping("/reload")
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Reload server caches, available only for admin")
  public void reloadCache() throws ServerError {
    try {
      cacheService.reloadAllCacheItems(true);
    } catch (RefreshCacheException e) {
      throw new ServerError(e);
    }
  }

}
