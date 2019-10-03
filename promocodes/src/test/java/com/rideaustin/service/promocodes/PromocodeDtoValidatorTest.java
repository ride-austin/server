package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindException;

import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.util.PromocodeBuilder;
import com.rideaustin.utils.RandomString;

public class PromocodeDtoValidatorTest {

  private static final String LONG_PROMO_CODE = "0123456789012345678901234567890";
  private static final double NEGATIVE_VAL = -10.00;
  private static final double BIG_VAL = 1000000000000.00;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private PromocodeDtoValidator testedInstance;
  @Mock
  private PromocodeServiceConfig config;
  private String promocodeLiteral;
  private BindException errors;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new PromocodeDtoValidator(config);
    promocodeLiteral = RandomString.generate();
    errors = new BindException(testedInstance, "promocode");
    Mockito.when(config.getMaximumPromocodeValue()).thenReturn(BigDecimal.valueOf(50.0));
  }

  @Test
  public void testValidateNoLiteral() throws RideAustinException {
    PromocodeDto promocode = PromocodeBuilder.create().asDto();
    testedInstance.validate(promocode, errors);
    assertError(errors, "Promocode cannot be empty");
  }

  @Test
  public void testValidateTooLong() throws RideAustinException {
    PromocodeDto promocode = PromocodeBuilder.create(LONG_PROMO_CODE).asDto();

    testedInstance.validate(promocode, errors);

    assertError(errors, "Promocode too long");
  }

  @Test
  public void testValidateNoValue() throws RideAustinException {
    PromocodeDto promocode = PromocodeBuilder.create(promocodeLiteral).asDto();

    testedInstance.validate(promocode, errors);

    assertTrue(errors.getAllErrors().stream().anyMatch(e -> e.getDefaultMessage().equals("Promocode value cannot be empty")));
  }

  @Test
  public void testValidateLessThanZero() throws RideAustinException {
    PromocodeDto promocode = PromocodeBuilder.create(promocodeLiteral).setValue(NEGATIVE_VAL).asDto();

    testedInstance.validate(promocode, errors);

    assertError(errors, "Value cannot be less than zero");
  }

  @Test
  public void testValidateGreaterThanMax() throws RideAustinException {
    PromocodeDto promocode = PromocodeBuilder.create(promocodeLiteral).setValue(BIG_VAL).asDto();
    testedInstance.validate(promocode, errors);

    assertError(errors, "Promocode value too high. Max value: 50.0");
  }

  private void assertError(BindException errors, String message) {
    assertTrue(errors.getAllErrors().stream().anyMatch(e -> e.getDefaultMessage().equals(message)));
  }

}