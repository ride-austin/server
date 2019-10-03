package com.rideaustin.rest.model;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.rideaustin.Constants;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class PagingParams {

  @ApiParam(value = "Page number", example = "1")
  private int page = 0;
  @ApiParam(value = "Columns to sort the result by, comma-separated list")
  private List<String> sort = Collections.singletonList("id");
  @ApiParam(value = "Order the result descending", example = "false")
  private boolean desc = false;
  @ApiParam(value = "Page size", example = "20")
  private int pageSize = Constants.PAGE_SIZE;

  private PageRequest toPageRequest(int size) {
    Sort sortObj = new Sort(desc ? Sort.Direction.DESC : Sort.Direction.ASC, sort);
    return new PageRequest(page, size, sortObj);
  }

  public PageRequest toPageRequest() {
    return toPageRequest(pageSize);
  }

}
