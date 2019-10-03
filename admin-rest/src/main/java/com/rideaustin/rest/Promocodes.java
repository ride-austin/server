package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.ListPromocodeDtoEnricher;
import com.rideaustin.assemblers.PromocodeDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListPromocodeDto;
import com.rideaustin.rest.model.ListPromocodeParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.service.promocodes.PromocodeDtoValidator;
import com.rideaustin.service.promocodes.PromocodeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/promocodes")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Promocodes {

  private final PromocodeService promocodeService;
  private final ListPromocodeDtoEnricher listPromocodeDtoEnricher;
  private final PromocodeDtoAssembler dtoAssembler;
  private final PromocodeDtoValidator validator;

  @InitBinder("promocodeDto")
  public void initBinder(WebDataBinder dataBinder) {
    dataBinder.addValidators(validator);
  }

  @GetMapping
  @WebClientEndpoint
  @ApiOperation(value = "List available promocodes", response = ListPromocodeDto.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Page<ListPromocodeDto> listPromocodes(
    @ApiParam @ModelAttribute ListPromocodeParams searchCriteria,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    Page<ListPromocodeDto> promocodes = promocodeService.listPromocodes(searchCriteria, paging);
    return promocodes.map(listPromocodeDtoEnricher);
  }

  @WebClientEndpoint
  @GetMapping("/{promocodeId}")
  @ApiOperation("Get promocode information")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public PromocodeDto getPromocode(
    @ApiParam(value = "Promocode ID", example = "1") @PathVariable Long promocodeId
  ) {
    return dtoAssembler.toDto(promocodeService.getPromocode(promocodeId));
  }

  @WebClientEndpoint
  @CheckedTransactional
  @ApiOperation("Add a new promocode")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid")
  })
  public PromocodeDto addPromocode(
    @ApiParam(required = true) @RequestBody @Valid PromocodeDto promocodeDto,
    BindingResult bindingResult
  ) throws RideAustinException {
    handleBindingErrors(bindingResult);
    return dtoAssembler.toDto(promocodeService.addPromocode(dtoAssembler.toDs(promocodeDto)));
  }

  @WebClientEndpoint
  @CheckedTransactional
  @PutMapping(value = "/{promocodeId}",
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Update existing promocode")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid")
  })
  public PromocodeDto updatePromocode(
    @ApiParam(value = "Promocode ID", example = "1") @PathVariable Long promocodeId,
    @ApiParam @RequestBody @Valid PromocodeDto promocodeDto,
    BindingResult bindingResult
  ) throws RideAustinException {
    handleBindingErrors(bindingResult);
    return dtoAssembler.toDto(promocodeService.updatePromocode(promocodeId, dtoAssembler.toDs(promocodeDto)));
  }

  @WebClientEndpoint
  @GetMapping("/{promocodeId}/usage")
  @ApiOperation("Get usage count for a promocode")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public long getUsageCount(@ApiParam(value = "Promocode ID", example = "1") @PathVariable long promocodeId) {
    return promocodeService.getUsageCount(promocodeId);
  }

  private void handleBindingErrors(BindingResult bindingResult) throws BadRequestException {
    if (bindingResult.getErrorCount() > 0) {
      throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }
  }

}
