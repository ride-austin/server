package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

public class CurrentUserServiceTest {

  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private SecurityContext context;
  @Mock
  private Authentication authentication;

  private CurrentUserService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CurrentUserService(userDslRepository);
  }

  @Test
  public void getUserReturnsUserOnPresentPrincipal() {
    final User user = new User();
    SecurityContextHolder.setContext(context);
    when(context.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);

    final User result = testedInstance.getUser();

    assertEquals(user, result);
  }

  @Test
  public void getUserReturnsNullOnAbsentPrincipal() {
    SecurityContextHolder.setContext(context);
    when(context.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(new Object());

    final User result = testedInstance.getUser();

    assertNull(result);
  }

  @Test
  public void getUserReturnsNullOnAbsentAuthentication() {
    SecurityContextHolder.setContext(context);
    when(context.getAuthentication()).thenReturn(null);

    final User result = testedInstance.getUser();

    assertNull(result);
  }

  @Test
  public void setUserSetsAuthentication() {
    final User user = new User();
    SecurityContextHolder.setContext(context);
    when(userDslRepository.getWithDependencies(anyLong())).thenReturn(user);

    testedInstance.setUser(user);

    verify(context, times(1)).setAuthentication(argThat(new BaseMatcher<Authentication>() {
      @Override
      public boolean matches(Object o) {
        final Authentication authentication = (Authentication) o;
        return authentication instanceof UsernamePasswordAuthenticationToken &&
          ((UsernamePasswordAuthenticationToken) o).getPrincipal().equals(user);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }
}