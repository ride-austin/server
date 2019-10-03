package com.rideaustin.rest;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.EmailVerificationService;
import com.rideaustin.service.GlobalExceptionEmailHelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@ApiIgnore
@RestController
@RequestMapping("/rest/bounce")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmailBounceEndpoint {

  private final ObjectMapper mapper;
  private final EmailVerificationService emailVerificationService;
  private final GlobalExceptionEmailHelper emailHelper;

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void handleBounce(HttpServletRequest request) throws ServerError {
    SNSNotification snsNotification = new SNSNotification();
    try {
      snsNotification = mapper.readValue(request.getReader(), SNSNotification.class);
      log.info("Got message from SNS:" + snsNotification);
      if (SNSNotification.NotificationType.valueOf(snsNotification.type.toUpperCase()) == SNSNotification.NotificationType.SUBSCRIPTIONCONFIRMATION) {
        log.info("Handling subscription confirmation:" + snsNotification.subscribeURL);
        emailHelper.processException(new ServerError(snsNotification.subscribeURL), request, "SNS Subscription");
        return;
      }
      SESNotification sesNotification = mapper.readValue(snsNotification.message, SESNotification.class);
      if (SESNotification.Type.valueOf(sesNotification.notificationType.toUpperCase()) == SESNotification.Type.BOUNCE
        && SESNotification.Bounce.Type.valueOf(sesNotification.bounce.bounceType.toUpperCase()) == SESNotification.Bounce.Type.PERMANENT) {
        for (SESNotification.Bounce.BouncedRecipient recipient : sesNotification.bounce.bouncedRecipients) {
          emailVerificationService.handleBounce(recipient.emailAddress);
        }
      }
    } catch (Exception e) {
      throw new ServerError("Error occurred at bounce endpoint. Request: " + snsNotification, e);
    }
  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  @ToString
  private static class SNSNotification {
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("SubscribeURL")
    private String subscribeURL;

    private enum NotificationType {
      SUBSCRIPTIONCONFIRMATION,
      NOTIFICATION
    }
  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  @ToString
  private static class SESNotification {
    private String notificationType;
    private Bounce bounce;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Bounce {
      private String bounceType;
      private List<BouncedRecipient> bouncedRecipients;

      @Getter
      @Setter
      @JsonIgnoreProperties(ignoreUnknown = true)
      private static class BouncedRecipient {
        private String emailAddress;
      }

      private enum Type {
        PERMANENT,
        TRANSIENT,
        UNDETERMINED
      }
    }

    private enum Type {
      BOUNCE,
      COMPLAINT,
      DELIVERY
    }
  }
}
