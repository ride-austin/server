package com.rideaustin.security;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserDslRepository userDslRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = userDslRepository.findByEmail(username);

    if (user == null) {
      throw new UsernameNotFoundException("No such user");
    }

    return user;
  }

}
