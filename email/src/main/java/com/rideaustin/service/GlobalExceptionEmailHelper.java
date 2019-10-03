package com.rideaustin.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.BasicEmail;
import com.rideaustin.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GlobalExceptionEmailHelper {

  private final Environment env;
  private final CurrentUserService currUserService;
  private final EmailService emailService;

  /**
   * Sends email notification about exception.
   * (also, see overloaded version which allows to specify additional information in Subject)
   *
   * @param serverError
   * @param req
   */
  public void processException(ServerError serverError, HttpServletRequest req) {
    processException(serverError, req, null);
  }

  @Async
  public void processExceptionAsync(ServerError serverError, HttpServletRequest req) {
    processException(serverError, req, null);
  }

  /**
   * Sends email notification about exception and allows to put additional information
   * to the Subject of email message.
   *
   * @param serverError the exception
   * @param req HttpServletRequest object, can be {@code null}
   * @param subjectSuffix additional text to be appended to email message subject, can be {@code null}
   */
  public void processException(ServerError serverError, HttpServletRequest req, String subjectSuffix) {
    log.error("Exception occurred", serverError);
    //    skip ClientAbortException - they're produced when client breaks connection and are useless in mailtrap
    Throwable cause = serverError.getCause();
    if (cause != null && cause.getClass().getName().contains("ClientAbortException")) {
      return;
    }
    try {
      User user = getUser();

      StringBuilder content = new StringBuilder();
      content
        .append("<html>")
        .append("<b>Timestamp: </b>").append(Instant.now()).append("<br>")
        .append("<b>User Email: </b>").append(user == null ? "None" : user.getEmail()).append("<br>")
        .append("<b>User Id: </b>").append(user == null ? "None" : user.getId()).append("<br>")
        .append("<b>Unique Id: </b>").append(serverError.getUniqueIdentifier()).append("<br>");
      if (req != null) {
        content.append("<b>API Invoked: </b>");
        content.append(req.getMethod()).append(' ').append(req.getRequestURI());
        if (req.getQueryString() != null) {
          content.append('?').append(req.getQueryString());
        }
        String userAgent = req.getHeader("User-Agent") != null ? req.getHeader("User-Agent") : "";
        content.append("<br>")
          .append("<b>User-Agent Header: </b>").append(userAgent).append("<br>");
        if ((req.getParameterMap() != null) && (req.getParameterMap().size() > 0)) {
          content.append("<b>Parameters: </b><br><small>");
          req.getParameterMap().forEach((key, value) -> content.append(" - ").append(key).append(": ").append(value[0]).append("<br>"));
          content.append("</small>");
        }
        if ((req.getHeaderNames() != null) && (req.getHeaderNames().hasMoreElements())) {
          content.append("<b>Headers: </b><br><small>");
          Enumeration<String> enumeration = req.getHeaderNames();
          while (enumeration.hasMoreElements()) {
            String headerKey = enumeration.nextElement();
            content.append(" - ").append(headerKey).append(": ").append(req.getHeader(headerKey)).append("<br>");
          }
          content.append("</small>");
        }
      }

      String stacktrace = getStackTrace(serverError);
      content
        .append("<b>Stacktrace: </b><pre>").append(stacktrace).append("</pre>")
        .append("</html>");
      // to
      String recipients = env.getProperty("rideaustin.developers.email",
        "bartosz.rybusinski@crossover.com,ruslan.romanov@crossover.com");
      // subject
      StringBuilder subject = new StringBuilder();
      subject.append(env.getProperty("environment", "DEV"));
      subject.append(": Exception");
      if (!StringUtils.isBlank(subjectSuffix)){
        subject.append(": ").append(subjectSuffix);
      }
      // send
      emailService.sendEmail(new BasicEmail(subject.toString(), content.toString(), recipients));
    } catch (Exception thr) {
      log.error("Error while sending developer email", thr);
    }
  }

  private String getStackTrace(ServerError serverError) {
    StringWriter sw = new StringWriter();
    (serverError.getCause() == null ? serverError : serverError.getCause()).printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  private User getUser() {
    try {
      return currUserService.getUser();
    } catch (Exception e) {
      log.error("Failed to get current user", e);
      return null;
    }
  }
}
