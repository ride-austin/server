package com.rideaustin.rest;

import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.assemblers.SupportTopicAssembler;
import com.rideaustin.assemblers.SupportTopicFormAssembler;
import com.rideaustin.model.SupportTopicForm;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.ListSupportTopicParams;
import com.rideaustin.rest.model.SupportTopicDto;
import com.rideaustin.rest.model.SupportTopicFormDto;
import com.rideaustin.service.SupportTopicService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/supporttopics")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SupportTopics {

  private final SupportTopicService supportTopicService;
  private final SupportTopicAssembler assembler;
  private final SupportTopicFormAssembler formAssembler;

  @GetMapping
  @ApiOperation("Get a list of available support topics")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<SupportTopicDto> listSupportTopics(
    @ApiParam @ModelAttribute ListSupportTopicParams searchCriteria) {
    return assembler.toDto(supportTopicService.listSupportTopics(searchCriteria));
  }

  @MobileClientEndpoint
  @GetMapping("/list/{avatarType}")
  @ApiOperation("Get a list of available support topics for an avatar type")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<SupportTopicDto> listByAvatarType(
    @ApiParam(value = "Avatar type", allowableValues = "RIDER,DRIVER") @PathVariable AvatarType avatarType
  ) {
    return assembler.toDto(supportTopicService.listParentSupportTopicsByAvatarType(avatarType));
  }

  @MobileClientEndpoint
  @GetMapping("/{parentTopicId}/children")
  @ApiOperation("Get a list of child support topics")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<SupportTopicDto> listByParent(
    @ApiParam(value = "Parent topic ID", example = "1") @PathVariable Long parentTopicId
  ) {
    ListSupportTopicParams params = ListSupportTopicParams.builder().parentTopicId(parentTopicId).build();
    return assembler.toDto(supportTopicService.listSupportTopics(params));
  }

  @MobileClientEndpoint
  @ApiOperation("Get a support topic form")
  @GetMapping(value = "/{supportTopicId}/form")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Support topic doesn't have a form")
  })
  public SupportTopicFormDto listForms(
    @ApiParam(value = "Support topic ID", example = "1") @PathVariable Long supportTopicId
  ) throws NotFoundException {
    SupportTopicForm form = supportTopicService.findForm(supportTopicId);
    if (form == null) {
      throw new NotFoundException("Support topic doesn't have forms attached");
    }
    return formAssembler.toDto(form);
  }

}
