package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.assemblers.CampaignDtoAssembler;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.rest.CampaignProviderDto;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.repo.dsl.CampaignProviderDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.CampaignDto;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/campaigns/")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Campaigns {

  private final CampaignDslRepository campaignDslRepository;
  private final CampaignProviderDslRepository campaignProviderDslRepository;

  private final CampaignDtoAssembler campaignDtoAssembler;

  @GetMapping(value = "{id}")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_ADMIN})
  @ApiOperation("Get information on given campaign")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Campaign is not found")
  })
  public CampaignDto getOne(@ApiParam(value = "Campaign ID", example = "1") @PathVariable long id) throws NotFoundException {
    final Campaign campaign = campaignDslRepository.findOne(id);
    if (campaign == null) {
      throw new NotFoundException("Campaign not found");
    }
    return campaignDtoAssembler.toDto(campaign);
  }

  @GetMapping(value = "providers")
  @ApiOperation("Get information on campaign providers")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_ADMIN})
  public List<CampaignProviderDto> listProviders(
    @ApiParam(value = "Campaign ID", example = "1", defaultValue = "1", required = true)
    @RequestParam(defaultValue = "1") long cityId
  ) {
    return campaignProviderDslRepository.getAll(cityId);
  }

  @GetMapping(value = "providers/{id}/campaigns")
  @ApiOperation("Get information on campaigns belonging to a given provider")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_ADMIN})
  public List<CampaignDto> listCampaignsByProvider(@ApiParam(value = "Campaign provider ID", example = "1") @PathVariable long id) {
    return campaignDtoAssembler.toDto(campaignDslRepository.findByProvider(id));
  }
}
