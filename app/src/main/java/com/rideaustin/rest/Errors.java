package com.rideaustin.rest;

import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.ExternalEndpoint;
import com.rideaustin.rest.model.ErrorMessageDto;
import com.rideaustin.rest.model.ErrorMessageDto.ReasonKey;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/errors")
public class Errors {

  @ExternalEndpoint
  @RequestMapping("/401")
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public String error401(ServletRequest request, HttpServletResponse response) {
    response.setHeader(
      ErrorMessageDto.REASON_KEY,
      Optional.ofNullable(request.getAttribute(ErrorMessageDto.REASON_KEY))
        .map(Object::toString)
        .map(ReasonKey::valueOf)
        .map(Enum::name)
        .orElse(ReasonKey.UNKNOWN.name())
    );

    return Optional.ofNullable(request.getAttribute(ErrorMessageDto.REASON)).map(Object::toString).orElse("");
  }

  @ExternalEndpoint
  @RequestMapping(value = "/413", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity error413() {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase());
  }

}