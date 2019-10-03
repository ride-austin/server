package com.rideaustin.test.actions.cascaded;

import org.springframework.test.web.servlet.ResultActions;

public interface ResultVerificationState {
  void verify(ResultActions resultActions) throws Exception;
}
