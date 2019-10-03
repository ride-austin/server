package com.rideaustin.service;

import static com.rideaustin.Constants.ErrorMessages.TERM_DRIVER_ACCEPTED_ALREADY;
import static com.rideaustin.Constants.ErrorMessages.TERM_NOT_FOUND_TEMPLATE;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.Terms;
import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.TermsDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.exception.RideAustinException;

@RunWith(MockitoJUnitRunner.class)
public class TermsServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @InjectMocks
  private TermsService termsService;

  @Mock
  private TermsDslRepository termsDslRepository;

  private Driver driver;

  private Terms terms;

  private final Long termId = 1L;

  @Before
  public void setUp() {
    driver = new Driver();
    terms = new Terms();
  }

  @Test
  public void shouldAcceptTerms() throws RideAustinException {
    when(termsDslRepository.getOne(anyLong())).thenReturn(terms);
    when(termsDslRepository.getTermsAcceptance(anyLong(), anyLong())).thenReturn(null);

    termsService.acceptTerms(driver, termId);
  }

  @Test
  public void shouldThrowException_WhenTermIsNotFound() throws RideAustinException {
    expectedException.expect(ConflictException.class);
    expectedException.expectMessage(String.format(TERM_NOT_FOUND_TEMPLATE, termId));
    when(termsDslRepository.getOne(anyLong())).thenReturn(null);

    termsService.acceptTerms(driver, termId);
  }

  @Test
  public void shouldThrowException_WhenTermIsAlreadyAccepted() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(TERM_DRIVER_ACCEPTED_ALREADY);
    when(termsDslRepository.getOne(anyLong())).thenReturn(terms);
    when(termsDslRepository.getTermsAcceptance(anyLong(), anyLong())).thenReturn(new TermsAcceptance());

    termsService.acceptTerms(driver, termId);
  }
}
