package com.rideaustin.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

public class UserDetailsServiceImplTest {

  private UserDetailsServiceImpl testedInstance;

  @Mock
  private UserDslRepository repository;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new UserDetailsServiceImpl(repository);
  }

  @Test
  public void loadUserByUsername() {
    final User user = new User();
    when(repository.findByEmail(anyString())).thenReturn(user);

    final UserDetails result = testedInstance.loadUserByUsername("email@test.com");

    assertEquals(user, result);
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testNotFound() {
    final UserDetails result = testedInstance.loadUserByUsername("email@test.com");
  }
}