package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.rest.CampaignRiderDto;
import com.rideaustin.model.rest.CampaignSubscriptionDto;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.service.CampaignService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/campaigns/")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CampaignsAdministration {

  private final CampaignService campaignService;
  private final CampaignDslRepository campaignDslRepository;


  @GetMapping
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("List campaigns that the rider is subscribed to")
  public List<CampaignSubscriptionDto> listSubscriptions(@ApiParam(value = "Rider ID", example = "1", required = true) @RequestParam("for") long riderId) {
    return campaignService.listSubscriptions(riderId);
  }

  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @GetMapping(value = "{id}/subscribers")
  @ApiOperation("List riders that are subscribed to a campaign")
  public List<CampaignRiderDto> listSubscribedRiders(@ApiParam(value = "Campaign ID", example = "1") @PathVariable long id) {
    return campaignDslRepository.listSubscribedRiders(id);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @PostMapping(value = "{id}/subscribers")
  @ApiOperation("Subscribe a rider to a campaign")
  public void subscribeRider(
    @ApiParam(value = "Campaign ID", example = "1") @PathVariable("id") long campaignId,
    @ApiParam(value = "Rider ID", example = "1", required = true) @RequestParam long riderId) {
    campaignService.subscribeRider(campaignId, riderId);
  }

  @ResponseStatus(HttpStatus.ACCEPTED)
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @DeleteMapping(value = "{id}/subscribers/{riderId}")
  @ApiOperation("Unsubscribe a rider from a campaign")
  public void unsubscribeRider(
    @ApiParam(value = "Campaign ID", example = "1")  @PathVariable("id") long campaignId,
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long riderId) {
    campaignService.unsubscribeRider(campaignId, riderId);
  }
}
