package com.rideaustin.assemblers;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.rideaustin.model.SupportTopicForm;
import com.rideaustin.rest.model.SupportTopicFormDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SupportTopicFormAssembler implements SingleSideAssembler<SupportTopicForm, SupportTopicFormDto> {

  private final ObjectMapper mapper;

  @Override
  public SupportTopicFormDto toDto(SupportTopicForm supportTopicForm) {
    SupportTopicFormDto.SupportTopicFormDtoBuilder builder = SupportTopicFormDto.builder()
      .actionTitle(supportTopicForm.getActionTitle())
      .actionType(supportTopicForm.getActionType())
      .body(supportTopicForm.getBody())
      .headerText(supportTopicForm.getHeaderText())
      .title(supportTopicForm.getTitle())
      .id(supportTopicForm.getParent().getId());

    TypeFactory typeFactory = mapper.getTypeFactory();
    CollectionType type = typeFactory.constructCollectionType(List.class, SupportTopicFormDto.Field.class);
    try {
      builder.supportFields(mapper.readValue(supportTopicForm.getFieldContent(), type));
    } catch (IOException ignore) {
      log.error("Failed to read field content", ignore);
    }
    return builder.build();
  }
}
