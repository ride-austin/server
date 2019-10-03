package com.rideaustin.rest;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.AppInfo;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PlatformType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.BuildInfo;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.AppInfoService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/rest/configs")
public class ConfigsAdministration {

  private static final String UNDEFINED = "undefined";
  private final AppInfoService appInfoService;

  private final String projectVersion;
  private final String buildNumber;
  private final String gitCommit;
  private final String gitBranch;

  public ConfigsAdministration(AppInfoService appInfoService, Environment env) {
    this.appInfoService = appInfoService;
    projectVersion = env.getProperty("ra.project.version", UNDEFINED);
    buildNumber = env.getProperty("ra.project.build", UNDEFINED);
    gitCommit = env.getProperty("ra.project.commit", UNDEFINED);
    gitBranch = env.getProperty("ra.project.branch", UNDEFINED);
  }

  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Create new application info")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Application information provided is invalid")
  })
  @PostMapping(path = "/app/info", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public AppInfo createAppInfo(@ApiParam(value = "Application info object", required = true) @RequestBody AppInfo info) throws BadRequestException {
    validateAppInfo(info);
    return appInfoService.createAppInfo(info);
  }

  @WebClientEndpoint
  @ApiOperation("Information on current server build")
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @GetMapping(path = "/build", produces = MediaType.APPLICATION_JSON_VALUE)
  public BuildInfo getBuildInfo() {
    return new BuildInfo(projectVersion, buildNumber, gitCommit, gitBranch);
  }

  @WebClientEndpoint
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Update application information as an admin")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Application information provided is invalid"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Application information not found")
  })
  @PutMapping(path = "/app/info/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public AppInfo updateAppInfo(
    @ApiParam(value = "App info ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Updated application info object") @RequestBody AppInfo info
  ) throws RideAustinException {
    validateAppInfo(info);
    return appInfoService.updateAppInfo(id, info);
  }

  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation("Delete application information as an admin")
  @DeleteMapping(path = "/app/info/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteAppInfo(@ApiParam(value = "Application info ID", example = "1") @PathVariable long id) {
    appInfoService.deleteAppInfo(id);
  }

  @WebClientEndpoint
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation(value = "Paginated list of application infos available", response = AppInfo.class)
  @GetMapping(path = "/app/info", produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<AppInfo> listAppInfo(
    @ApiParam(value = "Application type", allowableValues = "RIDER,DRIVER") @RequestParam(required = false) AvatarType avatarType,
    @ApiParam(value = "Platform type") @RequestParam(required = false) PlatformType platformType,
    @ApiParam(value = "City ID", required = true, example = "1") @RequestParam Long cityId,
    @ApiParam(value = "Search query - user agent or application version") @RequestParam(required = false) String search,
    @ApiParam @ModelAttribute PagingParams paging) {
    return appInfoService.listAppInfo(avatarType, platformType, cityId, search, paging);
  }

  private void validateAppInfo(@Nonnull AppInfo info) throws BadRequestException {
    if (info.getAvatarType() == null) {
      throw new BadRequestException("Avatar type is required");
    }
    if (info.getPlatformType() == null) {
      throw new BadRequestException("Platform type is required");
    }
    if (StringUtils.isBlank(info.getVersion())) {
      throw new BadRequestException("Version is required");
    }
    if (info.getCityId() == null) {
      throw new BadRequestException("City is required");
    }
    if (info.getMandatoryUpgrade() == null) {
      throw new BadRequestException("Mandatory upgrade flag is required");
    }
    if (StringUtils.isBlank(info.getUserAgentHeader())) {
      throw new BadRequestException("User agent header is required");
    }
  }
}
