package com.rideaustin.service;


import com.rideaustin.rest.exception.ServerError;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/rest/test-exception/", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class GlobalExceptionHandlerTestingController {

  @RequestMapping(path = "", method = RequestMethod.GET)
  public void get() throws ServerError {
    throw new ServerError("error");
  }
}
