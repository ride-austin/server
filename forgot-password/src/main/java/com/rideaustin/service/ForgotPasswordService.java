package com.rideaustin.service;

import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.PasswordVerificationToken;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.PasswordVerificationTokenDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.user.PasswordResetEmail;
import com.rideaustin.service.user.PasswordResetSuccessEmail;
import com.rideaustin.utils.CryptUtils;
import com.rideaustin.utils.RandomString;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ForgotPasswordService {

  private final Environment environment;
  private final CityService cityService;
  private final EmailService emailService;
  private final PasswordEncoder encoder;
  private final PasswordVerificationTokenDslRepository repository;
  private final UserDslRepository userDslRepository;
  private final CryptUtils cryptUtils;

  @Transactional
  public void sendPasswordReminderEmail(User user) throws ServerError {
    final PasswordVerificationToken token = repository.save(new PasswordVerificationToken(user.getEmail()));
    sendPasswordResetEmail(user, token.getToken());
  }

  @Transactional
  public boolean resetPassword(String token) {
    final PasswordVerificationToken verificationToken = repository.findToken(token);
    if (verificationToken == null) {
      return false;
    } else {
      String newPassword = RandomString.generate(6);
      final User user = userDslRepository.findAnyByEmail(verificationToken.getEmail());
      userDslRepository.changePassword(user.getId(), encoder.encode(cryptUtils.clientAppHash(verificationToken.getEmail(), newPassword)));
      try {
        emailService.sendEmail(new PasswordResetSuccessEmail(user, newPassword, cityService.getDefaultCity()));
      } catch (EmailException e) {
        log.error("Failed to send email", e);
        return false;
      }
      verificationToken.setExpiresOn(new Date());
      repository.save(verificationToken);
      return true;
    }
  }

  public void forceResetPassword(User user) throws ServerError {
    String newPassword = RandomString.generate(6);
    userDslRepository.changePassword(user.getId(), encoder.encode(cryptUtils.clientAppHash(user.getEmail(), newPassword)));
    try {
      emailService.sendEmail(new PasswordResetSuccessEmail(user, newPassword, cityService.getDefaultCity()));
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }

  private void sendPasswordResetEmail(User user, String token) throws ServerError {
    try {
      emailService.sendEmail(new PasswordResetEmail(user, token, environment.getProperty("ra.project.api-suffix", ""),
        cityService.getCityForCurrentClientAppVersionContext()));
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }
}
