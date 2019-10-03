package com.rideaustin.test.actions.cascaded;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.test.web.servlet.ResultActions;

public abstract class AbstractResultVerificationState implements ResultVerificationState {

  private boolean isVerbose = false;

  @Override
  public void verify(ResultActions resultActions) throws Exception {
    if (isVerbose) {
      verifyInternal(resultActions.andDo(print()));
    } else {
      verifyInternal(resultActions);
    }
  }

  public void withVerbose(boolean isVerbose){
    this.isVerbose = isVerbose;
  }

  protected abstract void verifyInternal(ResultActions resultActions) throws Exception;
}
