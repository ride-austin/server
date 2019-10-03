package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Payload;
import javax.validation.metadata.ConstraintDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.google.common.collect.ImmutableSet;
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
import com.twilio.exception.ApiConnectionException;

public class GlobalExceptionHandlerTest {

  @Mock
  private GlobalExceptionEmailHelper globalExceptionEmailHelper;

  private GlobalExceptionHandler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new GlobalExceptionHandler(globalExceptionEmailHelper);
  }

  @Test
  public void validationFailedResultsInBadRequestException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.validationFailed(new MethodArgumentNotValidException(null, new StubBindingResult(error)));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void validationFailedBindResultsInBadRequestException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.validationFailed(new BindException(new StubBindingResult(error)));

    assertTrue(result.getBody().contains(error));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void validationFailedConstraintResultsInBadRequestException() {
    final String error = "Error";

    final ConstraintViolationException constraintViolationException = new ConstraintViolationException(
      ImmutableSet.of(new StubConstraintViolation(error, Collections.emptySet()))
    );
    final ResponseEntity<String> result = testedInstance.validationFailed(constraintViolationException);

    assertTrue(result.getBody().contains(error));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void validationFailedConstraintResultsInAnnotationDefinedException() {
    final String error = "Error";

    final ConstraintViolationException constraintViolationException = new ConstraintViolationException(
      ImmutableSet.of(new StubConstraintViolation(error, Collections.singleton(StubReturnPayload.class)))
    );
    final ResponseEntity<String> result = testedInstance.validationFailed(constraintViolationException);

    assertTrue(result.getBody().contains(error));
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
  }

  @Test
  public void accessDeniedResultsInForbiddenException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.accessDenied(new AccessDeniedException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
  }

  @Test
  public void missingParameterResultsInBadRequestException() {
    final String parameter = "param";

    final ResponseEntity<String> result = testedInstance.missingParameter(new MissingServletRequestParameterException(parameter, null));

    assertEquals("Missing parameter "+parameter, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void missingRequestPartResultsInBadRequestException() {
    final String parameter = "param";

    final ResponseEntity<String> result = testedInstance.missingRequestPart(new MissingServletRequestPartException(parameter));

    assertEquals("Missing parameter "+parameter, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void badRequestException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.badRequestException(new BadRequestException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void signUpExceptionResultsInUnprocessableEntity() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.signUpException(new SignUpException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
  }

  @Test
  public void forbiddenException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.forbiddenException(new ForbiddenException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
  }

  @Test
  public void notFoundException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.notFoundException(new NotFoundException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  public void unauthorizedException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.unauthorizedException(new UnAuthorizedException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  public void conflictException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.conflictException(new ConflictException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
  }

  @Test
  public void twilioCallExceptionResultsInBadRequestException() {
    final ResponseEntity<String> result = testedInstance.twilioCallException(new TwilioCallException(new ApiConnectionException("")));

    assertEquals("Failed to initiate call", result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void twilioSMSExceptionResultsInBadRequestException() {
    final ResponseEntity<String> result = testedInstance.twilioSMSException(new TwilioSMSException(new ApiConnectionException("")));

    assertEquals("Failed to send text message", result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void methodArgumentTypeMismatchExceptionResultsInBadRequestException() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.methodArgumentTypeMismatchException(
      new MethodArgumentTypeMismatchException("A", Object.class, "B", null, null)
    );

    assertEquals("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Object'", result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void pendingPaymentExceptionResultsInPaymentRequired() {
    final String error = "Error";

    final ResponseEntity<String> result = testedInstance.pendingPaymentException(new PendingPaymentException(error));

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.PAYMENT_REQUIRED, result.getStatusCode());
  }

  @Test
  public void serverErrorResultsInInternalServerError() {
    final String error = "Error";
    HttpServletRequest request = mock(HttpServletRequest.class);

    final ServerError serverError = new ServerError(error);
    final ResponseEntity<String> result = testedInstance.serverError(serverError, request);

    assertEquals(error, result.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    verify(globalExceptionEmailHelper).processExceptionAsync(serverError, request);
  }

  @Test
  public void genericExceptionResultsInInternalServerError() {
    final String error = "Error";
    HttpServletRequest request = mock(HttpServletRequest.class);

    final ResponseEntity<String> result = testedInstance.serverError(new Exception(error), request);

    assertTrue(result.getBody().startsWith("Something went wrong on server"));
    assertTrue(result.getBody().endsWith(error));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    verify(globalExceptionEmailHelper).processExceptionAsync(any(ServerError.class), eq(request));
  }

  @WhenInvalidReturn(HttpStatus.UNPROCESSABLE_ENTITY)
  private static class StubReturnPayload implements Payload {

  }

  private static class StubConstraintViolation implements ConstraintViolation<Object> {

    private final String message;
    private final Set<Class<? extends Payload>> payload;

    private StubConstraintViolation(String message, Set<Class<? extends Payload>> payload) {
      this.message = message;
      this.payload = payload;
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public String getMessageTemplate() {
      return null;
    }

    @Override
    public Object getRootBean() {
      return null;
    }

    @Override
    public Class<Object> getRootBeanClass() {
      return null;
    }

    @Override
    public Object getLeafBean() {
      return null;
    }

    @Override
    public Object[] getExecutableParameters() {
      return new Object[0];
    }

    @Override
    public Object getExecutableReturnValue() {
      return null;
    }

    @Override
    public Path getPropertyPath() {
      return null;
    }

    @Override
    public Object getInvalidValue() {
      return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
      return new ConstraintDescriptor<Annotation>() {
        @Override
        public Annotation getAnnotation() {
          return null;
        }

        @Override
        public String getMessageTemplate() {
          return null;
        }

        @Override
        public Set<Class<?>> getGroups() {
          return null;
        }

        @Override
        public Set<Class<? extends Payload>> getPayload() {
          return payload;
        }

        @Override
        public ConstraintTarget getValidationAppliesTo() {
          return null;
        }

        @Override
        public List<Class<? extends ConstraintValidator<Annotation, ?>>> getConstraintValidatorClasses() {
          return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
          return null;
        }

        @Override
        public Set<ConstraintDescriptor<?>> getComposingConstraints() {
          return null;
        }

        @Override
        public boolean isReportAsSingleViolation() {
          return false;
        }
      };
    }

    @Override
    public <U> U unwrap(Class<U> aClass) {
      return null;
    }
  }

  private static class StubBindingResult extends AbstractBindingResult {

    private final String message;

    protected StubBindingResult(String message) {
      super(null);
      this.message = message;
    }

    @Override
    public Object getTarget() {
      return null;
    }

    @Override
    protected Object getActualFieldValue(String s) {
      return null;
    }

    @Override
    public FieldError getFieldError() {
      return new FieldError("A", "B", message);
    }
  }
}