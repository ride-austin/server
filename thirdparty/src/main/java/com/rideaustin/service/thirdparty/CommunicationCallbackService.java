package com.rideaustin.service.thirdparty;

import com.rideaustin.model.thirdparty.CallbackResponse;
import com.rideaustin.rest.exception.ServerError;

public interface CommunicationCallbackService {

  CallbackResponse createCallbackResponse(String callSid, String from) throws ServerError;

  CallbackResponse createSmsCallbackResponse(String messageSid, String from, String body) throws ServerError;
}
