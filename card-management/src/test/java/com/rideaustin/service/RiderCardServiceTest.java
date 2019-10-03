package com.rideaustin.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.RiderCardLock;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RiderCardDslRepository;
import com.rideaustin.repo.dsl.RiderCardLockDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.model.UpdateCardDto;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.service.user.RiderService;

@RunWith(MockitoJUnitRunner.class)
public class RiderCardServiceTest {

  private static final String FINGERPRINT = "fingerprint";
  private static final long RIDER_ID = 1L;
  private static final long CARD_ID = 1L;
  private static final String CARD_TOKEN = "cardToken";
  private static final String STRIPE_ID = "StripeId";
  private static final Ride RIDE = new Ride();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private StripeService stripeService;
  @Mock
  private RiderService riderService;

  @Mock
  private RiderCardDslRepository riderCardRepository;
  @Mock
  private RiderCardLockDslRepository riderCardLockDslRepository;
  @Mock
  private RiderDslRepository riderDslRepository;

  @Mock
  private CityCache cityCache;

  @InjectMocks
  private RiderCardService riderCardService;

  private Rider rider;
  private RiderCard card;

  @Before
  public void setup() throws RideAustinException {
    when(riderCardLockDslRepository.findByFingerprint(anyString())).thenReturn(null);
    card = mockRiderCard(FINGERPRINT);
    card.setPrimary(true);
    User user = new User();
    rider = new Rider();
    rider.setId(1L);
    rider.setUser(user);
    rider.setPrimaryCard(card);
    rider.setStripeId(STRIPE_ID);
    card.setRider(rider);
    when(riderCardRepository.findOne(anyLong())).thenReturn(card);
    when(riderService.findRider(anyLong())).thenReturn(rider);
  }

  @Test
  public void testCheckIfLockedNoLock() {
    RiderCard card = mockRiderCard(FINGERPRINT);
    Boolean locked = riderCardService.checkIfCardLocked(card);
    assertThat(locked, equalTo(Boolean.FALSE));
  }

  @Test
  public void testCheckIfLockedNoFingerprint() {
    RiderCard card = mockRiderCard(null);
    Boolean locked = riderCardService.checkIfCardLocked(card);
    assertThat(locked, equalTo(Boolean.FALSE));
  }

  @Test
  public void testCheckIfLockedLock() {
    RiderCard card = mockRiderCard(FINGERPRINT);
    when(riderCardLockDslRepository.findByFingerprint(anyString())).thenReturn(new RiderCardLock());
    Boolean locked = riderCardService.checkIfCardLocked(card);
    assertThat(locked, equalTo(Boolean.TRUE));
  }

  @Test
  public void testCheckIfLockedWithFail() throws BadRequestException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Your credit card has been locked by your credit card company - please contact");
    RiderCard card = mockRiderCard(FINGERPRINT);
    when(riderCardLockDslRepository.findByFingerprint(anyString())).thenReturn(new RiderCardLock());
    riderCardService.checkIfCardLockedWithFail(card);
  }

  @Test
  public void testDeleteCardActiveRide() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    when(riderDslRepository.getActiveRide(anyObject())).thenReturn(new Ride());
    riderCardService.deleteRiderCard(RIDER_ID, CARD_ID);
  }

  @Test
  public void testDeleteCardNotFound() throws RideAustinException {
    expectedException.expect(NotFoundException.class);
    when(riderCardRepository.findOne(anyLong())).thenReturn(null);
    riderCardService.deleteRiderCard(RIDER_ID, CARD_ID);
  }

  @Test
  public void testDeleteCardPrimary() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("You cannot delete your primary credit card");
    riderCardService.deleteRiderCard(RIDER_ID, CARD_ID);
  }

  @Test
  public void testDeleteCardLocked() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Your credit card has been locked by your credit card company - please contact");
    when(riderCardLockDslRepository.findByFingerprint(anyString())).thenReturn(new RiderCardLock());
    riderCardService.deleteRiderCard(RIDER_ID, CARD_ID);
  }

  @Test
  public void testDeleteCardSuccess() throws RideAustinException {
    card.setPrimary(false);
    rider.setPrimaryCard(null);
    riderCardService.deleteRiderCard(RIDER_ID, CARD_ID);
    verify(stripeService, times(1)).deleteCardForRider(card);
    verify(riderCardRepository, times(1)).save(card);
    assertThat(card.isRemoved(), equalTo(Boolean.TRUE));
  }

  @Test
  public void testUpdateCardLocked() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Your credit card has been locked by your credit card company - please contact");
    when(riderCardLockDslRepository.findByFingerprint(anyString())).thenReturn(new RiderCardLock());
    riderCardService.updateRiderCard(RIDER_ID, new UpdateCardDto(CARD_ID, true));
  }

  @Test
  public void testUpdateCardSuccessNotPrimary() throws RideAustinException {
    riderCardService.updateRiderCard(RIDER_ID, new UpdateCardDto(CARD_ID, false));
  }

  @Test
  public void testUpdateCardSuccessNoLock() throws RideAustinException {
    riderCardService.updateRiderCard(RIDER_ID, new UpdateCardDto(CARD_ID, true));
  }

  @Test
  public void testUpdateCardActiveRide() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    when(riderDslRepository.getActiveRide(anyObject())).thenReturn(new Ride());
    riderCardService.updateRiderCard(RIDER_ID, new UpdateCardDto(CARD_ID, true));
  }

  @Test
  public void testAddCardActiveRide() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    when(riderDslRepository.getActiveRide(anyObject())).thenReturn(new Ride());
    riderCardService.addRiderCard(RIDER_ID, CARD_TOKEN);
  }

  @Test
  public void testAddCardCardLocked() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Your credit card has been locked by your credit card company - please contact");
    when(riderCardLockDslRepository.findByFingerprint(anyString())).thenReturn(new RiderCardLock());
    when(stripeService.createCardForRider(anyObject(), anyString())).thenReturn(card);
    riderCardService.addRiderCard(RIDER_ID, CARD_TOKEN);
    verify(stripeService, times(1)).createCardForRider(rider, CARD_TOKEN);
    verify(stripeService, times(1)).deleteCardForRider(card);
  }

  @Test
  public void testAddCardSuccess() throws RideAustinException {
    when(stripeService.createCardForRider(anyObject(), anyString())).thenReturn(card);
    riderCardService.addRiderCard(RIDER_ID, CARD_TOKEN);
    verify(riderCardRepository, times(1)).save(card);
  }

  @Test
  public void testLogCardNoFingerprint() {
    RiderCard card = mockRiderCard(null);
    riderCardService.lockCard(card, RIDE);
  }

  @Test
  public void testLogCardAlreadyLocked() {
    RiderCard card = mockRiderCard(FINGERPRINT);
    when(riderCardLockDslRepository.findByFingerprintAndRide(anyString(), anyObject())).thenReturn(new RiderCardLock());
    riderCardService.lockCard(card, RIDE);
    verify(riderCardLockDslRepository, never()).save(card);
  }

  @Test
  public void testLogCardNewLock() {
    RiderCard card = mockRiderCard(FINGERPRINT);
    when(riderCardLockDslRepository.findByFingerprintAndRide(anyString(), anyObject())).thenReturn(null);
    riderCardService.lockCard(card, RIDE);
    verify(riderCardLockDslRepository, times(1)).save(any(RiderCardLock.class));
  }

  @Test
  public void testListRiderCardsRequestsStripeIfLastSyncWasLaterThanDayAgo() throws Exception {
    when(riderCardRepository.findByRider(rider))
      .thenReturn(Collections.singletonList(RiderCard.builder().syncDate(DateUtils.addDays(new Date(), -2)).build()));

    riderCardService.listRiderCards(rider.getId());

    verify(stripeService, times(1)).listRiderCards(eq(rider), anyListOf(RiderCard.class));
  }

  @Test
  public void testListRiderCardsWontRequestStripeIfLastSyncWasNotLaterThanDayAgo() throws Exception {
    when(riderCardRepository.findByRider(rider))
      .thenReturn(Collections.singletonList(RiderCard.builder().syncDate(Date.from(Instant.now().minus(30, ChronoUnit.MINUTES))).build()));

    riderCardService.listRiderCards(rider.getId());

    verify(stripeService, never()).listRiderCards(eq(rider), anyListOf(RiderCard.class));
  }

  @Test
  public void testListRiderCardsOmitsCardsWithNullSyncDate() throws Exception {
    when(riderCardRepository.findByRider(rider))
      .thenReturn(Collections.singletonList(RiderCard.builder().syncDate(null).build()));

    riderCardService.listRiderCards(rider.getId());

    verify(stripeService, times(1)).listRiderCards(eq(rider), anyListOf(RiderCard.class));
  }

  private RiderCard mockRiderCard(String fingerprint) {
    RiderCard card = new RiderCard();
    card.setFingerprint(fingerprint);
    return card;

  }

}