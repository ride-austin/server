package com.rideaustin.dispatch.guards;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.model.user.User;
import com.rideaustin.service.CurrentUserService;

public class AuthorizedAdminGuardTest extends PersistingContextSupport {

  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private User user;
  @InjectMocks
  private AuthorizedAdminGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new AuthorizedAdminGuard();
    MockitoAnnotations.initMocks(this);

    when(currentUserService.getUser()).thenReturn(user);
  }

  @Test
  public void testEvaluateFalseWhenUserIsRider() {
    when(user.isAdmin()).thenReturn(false);
    when(user.isRider()).thenReturn(true);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenUserIsDriver() {
    when(user.isAdmin()).thenReturn(false);
    when(user.isDriver()).thenReturn(true);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateTrueWhenUserIsAdmin() {
    when(user.isAdmin()).thenReturn(true);

    boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}