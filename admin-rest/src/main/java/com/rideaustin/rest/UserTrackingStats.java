package com.rideaustin.rest;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.user.tracking.model.UserTrackStatsDto;
import com.rideaustin.user.tracking.repo.dsl.UserTrackDslRepository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/reports/trackingReport")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserTrackingStats {

  private final UserTrackDslRepository repository;

  @GetMapping
  @ApiOperation(value = "Get report about users who signed up via promo campaign", response = UserTrackStatsDto.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Page<UserTrackStatsDto> getAll(
    @ApiParam("Report start date") @RequestParam(required = false, value = "completedOnAfter") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
    @ApiParam("Report end date") @RequestParam(required = false, value = "completedOnBefore") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
    @ApiParam PagingParams pagingParams
  ) {
    Date from = Optional.ofNullable(start).map(Date::from).orElse(new Date(0));
    Date to = Optional.ofNullable(end).map(Date::from).orElse(new Date());
    return repository.findStatsForPeriod(from, to, pagingParams);
  }
}
