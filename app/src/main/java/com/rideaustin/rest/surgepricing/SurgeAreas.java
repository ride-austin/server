package com.rideaustin.rest.surgepricing;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.surgepricing.SurgePricingService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/surgeareas")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgeAreas {

  private final SurgePricingService surgePricingService;

  @GetMapping
  @WebClientEndpoint
  @MobileClientEndpoint
  @ApiOperation(value = "Get a paginated list of active surge areas", responseContainer = "List", response = RedisSurgeArea.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Page<RedisSurgeArea> listSurgeAreas(
    @ApiParam @ModelAttribute ListSurgeAreaParams searchCriteria,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    return surgePricingService.listSurgeAreas(searchCriteria, paging);
  }

}
