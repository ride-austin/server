package com.rideaustin.test.actions.cascaded;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.ResultActions;

public class SuccessVerificationState extends AbstractResultVerificationState {
  @Override
  public void verifyInternal(ResultActions resultActions) throws Exception {
    resultActions.andExpect(status().isOk());
  }
}
