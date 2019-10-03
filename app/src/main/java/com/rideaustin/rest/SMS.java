package com.rideaustin.rest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.ExternalEndpoint;
import com.rideaustin.model.thirdparty.CallbackResponse;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.thirdparty.CommunicationCallbackService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Transactional
@RestController
public class SMS {

  private final CommunicationCallbackService callbackService;

  @Inject
  public SMS(CommunicationServiceFactory serviceFactory) {
    this.callbackService = serviceFactory.createCallbackService();
  }

  @ExternalEndpoint
  @RequestMapping("/rest/sms/callback")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity callback(HttpServletRequest request) throws RideAustinException {
    String callSid = request.getParameter("CallSid");
    String from = request.getParameter("From");
    CallbackResponse response;
    if (callSid == null) {
      String messageSid = request.getParameter("MessageSid");
      String body = request.getParameter("Body");
      if (body.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      response = callbackService.createSmsCallbackResponse(messageSid, from, body);
    } else {
      response = callbackService.createCallbackResponse(callSid, from);
    }
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(response);
  }


}
