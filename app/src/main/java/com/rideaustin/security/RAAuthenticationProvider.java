package com.rideaustin.security;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;

import com.rideaustin.utils.CryptUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RAAuthenticationProvider extends DaoAuthenticationProvider {

  private final CryptUtils cryptUtils;

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) {
    super.additionalAuthenticationChecks(userDetails, convertAuthenticationTokenIfNeeded(userDetails, authentication));
  }

  private UsernamePasswordAuthenticationToken convertAuthenticationTokenIfNeeded(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) {
    if (authentication.getCredentials() != null) {
      return createHashedPassword(userDetails, authentication);
    }

    return authentication;
  }

  private UsernamePasswordAuthenticationToken createHashedPassword(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) {
    Object principal = authentication.getPrincipal();
    Object credentials = authentication.getCredentials();
    credentials = cryptUtils.clientAppHash(userDetails.getUsername(), String.valueOf(credentials));
    return new UsernamePasswordAuthenticationToken(principal, credentials);
  }
}
