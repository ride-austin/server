package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.lostandfound.LostAndFoundRequestDto;
import com.rideaustin.service.LostAndFoundService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/lostandfound")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LostAndFoundAdministration {

  private final LostAndFoundService lostAndFoundService;

  @GetMapping("{avatarId}/requests")
  @ApiOperation("Get a list of all lost/found item requests per user")
  public List<LostAndFoundRequestDto> requests(
    @ApiParam(value = "Avatar ID", example = "1") @PathVariable Long avatarId
  ) {
    return lostAndFoundService.findRequests(avatarId);
  }
}
