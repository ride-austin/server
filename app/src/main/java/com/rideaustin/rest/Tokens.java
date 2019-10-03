package com.rideaustin.rest;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.filter.ClientAppVersionContext;
import com.rideaustin.model.Token;
import com.rideaustin.model.Token.TokenEnvironment;
import com.rideaustin.model.Token.TokenType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.notifications.PushNotificationsFacade;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CheckedTransactional
@RequestMapping("/rest/tokens")
public class Tokens {

  private CurrentUserService currentUserService;
  private TokenRepository tokenRepo;
  private PushNotificationsFacade pushNotificationsFacade;
  private TokenEnvironment tokenEnvironment;

  @Inject
  public Tokens(CurrentUserService currentUserService, TokenRepository tokenRepo,
    PushNotificationsFacade pushNotificationsFacade, Environment environment) {
    this.currentUserService = currentUserService;
    this.tokenRepo = tokenRepo;
    this.pushNotificationsFacade = pushNotificationsFacade;
    tokenEnvironment = TokenEnvironment.valueOf(environment.getProperty("environment",
      String.class, "DEV"));
  }

  @MobileClientEndpoint
  @ApiOperation("Subscribe to push notifications")
  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to subscribe")
  })
  public void post(
    @ApiParam(value = "Token value", required = true) @RequestParam String value,
    @ApiParam("Token type") @RequestParam TokenType type,
    @ApiParam(value = "Avatar type", allowableValues = "RIDER,DRIVER", required = true, defaultValue = "RIDER")
    @RequestParam(defaultValue = AvatarType.NAME_RIDER) AvatarType avatarType
  ) throws RideAustinException {

    try {
      User user = currentUserService.getUser();
      Token token = new Token(value, tokenEnvironment.getValue(), type.getValue(),
        ClientAppVersionContext.getAppVersion().getClientAgentCity(), user, avatarType);
      token.setApplicationId(pushNotificationsFacade.deriveApplicationId(token));
      String arn = pushNotificationsFacade.subscribeToken(token);
      token.setArn(arn);

      tokenRepo.saveAndFlush(token);
    } catch (DataIntegrityViolationException e) {
      if (e.getCause() instanceof ConstraintViolationException) {
        log.debug("Attempt to save duplicate token", e);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return;
      }
      throw e;
    }
  }

}
