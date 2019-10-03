package com.rideaustin.rest;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.RiderPromocodeDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.PromocodeRedemptionDTO;
import com.rideaustin.rest.model.RiderPromoCodeDto;
import com.rideaustin.service.promocodes.PromocodeService;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/riders")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RiderPromocodes {

  private final PromocodeService promocodeService;
  private final RiderPromocodeDtoAssembler dtoAssembler;

  @RiderEndpoint
  @CheckedTransactional
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Get information about personal promocode")
  @GetMapping(path = "/{riderId}/promocode", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Invalid promocode")
  })
  public RiderPromoCodeDto getPromocode(@PathVariable Long riderId) throws BadRequestException {
    return dtoAssembler.toDto(promocodeService.getRiderPromocode(riderId));
  }

  @RiderEndpoint
  @CheckedTransactional
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Redeem promocode")
  @PostMapping(path = "/{riderId}/promocode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Invalid promocode")
  })
  public RiderPromoCodeDto usePromocode(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable Long riderId,
    @ApiParam("Promocode object") @RequestBody RedeemPromocodeDto promocode
  ) throws RideAustinException {
      RiderPromoCodeDto promoCodeDto = dtoAssembler.toDto(promocodeService.applyPromocode(riderId, promocode.getCodeLiteral()));
      promoCodeDto.setRemainingCredit(promocodeService.getTotalCreditForRider(riderId));
      return promoCodeDto;
  }

  @RiderEndpoint
  @ApiOperation("Get remaining free credit")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_ADMIN})
  @GetMapping(path = "/{riderId}/promocode/remainder", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Rider is not permitted to view other rider's remainder")
  })
  public RemainderResponse getRemainder(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable Long riderId
  ) throws ForbiddenException {
    return new RemainderResponse(safeZero(promocodeService.getTotalCreditForRider(riderId)));
  }

  @RiderEndpoint
  @ApiOperation("Get a list of promocode redemptions")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_ADMIN})
  @GetMapping(path = "/{riderId}/promocode/redemptions", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Rider is not permitted to view other rider's redemption history")
  })
  public List<PromocodeRedemptionDTO> getRedemptions(@PathVariable Long riderId) throws ForbiddenException {
    return promocodeService.getRedemptionsForRider(riderId);
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  private static class RemainderResponse {
    @ApiModelProperty(required = true)
    private final BigDecimal remainder;
  }

  @Getter
  @Setter
  @ApiModel
  private class RedeemPromocodeDto {
    @ApiModelProperty(required = true)
    private String codeLiteral;
  }
}
