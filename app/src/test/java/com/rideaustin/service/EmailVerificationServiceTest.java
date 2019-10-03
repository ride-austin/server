package com.rideaustin.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

public class EmailVerificationServiceTest {

  @Mock
  private UserDslRepository userDslRepository;

  private EmailVerificationService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new EmailVerificationService(userDslRepository);
  }

  @Test
  public void handleBounceUnverifiesEmail() {
    final User user = new User();
    user.setEmailVerified(true);
    when(userDslRepository.findAnyByEmail(anyString())).thenReturn(user);

    testedInstance.handleBounce("ssfdf@dfdf.ee");

    assertFalse(user.isEmailVerified());
    verify(userDslRepository, times(1)).save(eq(user));
  }
}