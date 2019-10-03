package com.rideaustin.service;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.repo.dsl.UserDslRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmailVerificationService {

  private final UserDslRepository userDslRepository;

  @Transactional
  public void handleBounce(String email) {
    Optional
      .ofNullable(userDslRepository.findAnyByEmail(email))
      .ifPresent(
        u -> {
          u.setEmailVerified(false);
          userDslRepository.save(u);
        }
      );
  }

}