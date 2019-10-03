package com.rideaustin.service;

import static java.util.stream.Collectors.joining;

import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Payload;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.rideaustin.rest.WhenInvalidReturn;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.PendingPaymentException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.exception.SignUpException;
import com.rideaustin.rest.exception.UnAuthorizedException;
import com.rideaustin.service.thirdparty.TwilioCallException;
import com.rideaustin.service.thirdparty.TwilioSMSException;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GlobalExceptionHandler {

  private final GlobalExceptionEmailHelper globalExceptionEmailHelper;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> validationFailed(MethodArgumentNotValidException e) {
    return badRequestException(new BadRequestException(e.getBindingResult().getFieldError().getDefaultMessage()));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<String> validationFailed(BindException e) {
    return badRequestException(new BadRequestException(getMessage(e.getBindingResult())));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> validationFailed(ConstraintViolationException e) {
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      Set<Class<? extends Payload>> payloads = violation.getConstraintDescriptor().getPayload();
      for (Class<? extends Payload> payload : payloads) {
        WhenInvalidReturn annotation = payload.getAnnotation(WhenInvalidReturn.class);
        if (annotation != null) {
          return ResponseEntity.status(annotation.value()).body(violation.getMessage());
        }
      }
    }
    return badRequestException(
      new BadRequestException(
        e.getConstraintViolations()
          .stream()
          .map(cv -> String.format("%s", cv.getMessage()))
          .collect(joining(", "))
      )
    );
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> accessDenied(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<String> missingParameter(MissingServletRequestParameterException e) {
    return ResponseEntity.badRequest().body("Missing parameter " + e.getParameterName());
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<String> missingRequestPart(MissingServletRequestPartException e) {
    return ResponseEntity.badRequest().body("Missing parameter " + e.getRequestPartName());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<String> badRequestException(BadRequestException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  @ExceptionHandler(SignUpException.class)
  public ResponseEntity<String> signUpException(SignUpException e) {
    return ResponseEntity.unprocessableEntity().body(e.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<String> forbiddenException(ForbiddenException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<String> notFoundException(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(UnAuthorizedException.class)
  public ResponseEntity<String> unauthorizedException(UnAuthorizedException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<String> conflictException(ConflictException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
  }

  @ExceptionHandler(TwilioCallException.class)
  public ResponseEntity<String> twilioCallException(TwilioCallException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  @ExceptionHandler(TwilioSMSException.class)
  public ResponseEntity<String> twilioSMSException(TwilioSMSException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<String> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  @ExceptionHandler(PendingPaymentException.class)
  public ResponseEntity<String> pendingPaymentException(PendingPaymentException e) {
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());
  }

  @ExceptionHandler(ServerError.class)
  public ResponseEntity<String> serverError(ServerError e, HttpServletRequest req) {
    globalExceptionEmailHelper.processExceptionAsync(e, req);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> serverError(Exception e, HttpServletRequest req) {
    return serverError(new ServerError(e), req);
  }

  private String getMessage(BindingResult bindingResult) {
    FieldError fieldError = bindingResult.getFieldError();
    if (fieldError != null) {
      return String.format("%s %s", StringUtils.capitalize(fieldError.getField()), fieldError.getDefaultMessage());
    }
    return bindingResult.getAllErrors().get(0).getDefaultMessage();
  }

}
