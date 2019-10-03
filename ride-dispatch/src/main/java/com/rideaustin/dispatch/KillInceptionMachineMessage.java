package com.rideaustin.dispatch;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KillInceptionMachineMessage implements Serializable {

  private static final long serialVersionUID = 949816168165164343L;

  private Long rideId;

}
