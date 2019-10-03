package com.rideaustin.service;

import javax.inject.Inject;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrentUserService {

  private final UserDslRepository userDslRepository;

  public User getUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof User) {
        return (User) principal;
      }
    }

    return null;
  }

  public void setUser(final User user) {
    User dbUser = userDslRepository.getWithDependencies(user.getId());
    Authentication authentication = new UsernamePasswordAuthenticationToken(dbUser, null, dbUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
