package com.rideaustin.events.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.events.OnboardingUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.onboarding.OnboardingStatusCheck;

public class OnboardingUpdateEventListenerTest {

  private static final long DRIVER_ID = 1L;
  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private ApplicationContext applicationContext;
  @Spy
  private Driver driver;
  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;
  @Mock
  private CurrentUserService currentUserService;

  private OnboardingUpdateEventListener testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new OnboardingUpdateEventListener(documentDslRepository, carDocumentDslRepository, driverDslRepository, currentUserService);
  }

  @Test
  public void testHandleDriverUpdateDoesntUpdateDriverOnboardingStatusIfNoChangesAreDetectedByChecks() {
    initListener(new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.NOT_CHANGED));
    when(driverDslRepository.findById(anyLong())).thenReturn(new Driver());

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driverDslRepository, Mockito.never()).save(any(Driver.class));
  }

  @Test
  public void testHandleDriverUpdateUnsetsDriverPendingSinceFieldOnGettingFinalReview() {
    initListener(new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.FINAL_REVIEW));
    Mockito.when(driverDslRepository.findById(Matchers.eq(DRIVER_ID))).thenReturn(driver);

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driver).setOnboardingStatus(DriverOnboardingStatus.FINAL_REVIEW);
    Mockito.verify(driver).setOnboardingPendingSince((Date) Matchers.isNull());
    Mockito.verify(driverDslRepository).saveAs(Matchers.eq(driver), any());
  }

  @Test
  public void testHandleDriverUpdateSetsDriverPendingSinceFieldOnGettingPending() {
    initListener(new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.PENDING));
    Mockito.when(driverDslRepository.findById(Matchers.eq(DRIVER_ID))).thenReturn(driver);

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driver).setOnboardingStatus(DriverOnboardingStatus.PENDING);
    Mockito.verify(driver).setOnboardingPendingSince(any(Date.class));
    Mockito.verify(driverDslRepository).saveAs(Matchers.eq(driver), any());
  }

  @Test
  public void shouldNotSetDriverPendingOnNoChange() {
    initListener(new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.PENDING));
    Mockito.when(driverDslRepository.findById(Matchers.eq(DRIVER_ID))).thenReturn(driver);
    Mockito.when(driver.getOnboardingStatus()).thenReturn(DriverOnboardingStatus.PENDING);

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driver, Mockito.never()).setOnboardingStatus(any());
    Mockito.verify(driver, Mockito.never()).setOnboardingPendingSince(any(Date.class));
    Mockito.verify(driverDslRepository).saveAs(Matchers.eq(driver), any());
  }

  @Test
  public void testHandleDriverUpdateSetsOnboardingStatusOnGettingRejected() {
    initListener(new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.REJECTED));
    Mockito.when(driverDslRepository.findById(Matchers.eq(DRIVER_ID))).thenReturn(driver);

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driver).setOnboardingStatus(DriverOnboardingStatus.REJECTED);
    Mockito.verify(driver, Mockito.never()).setOnboardingPendingSince(any(Date.class));
    Mockito.verify(driverDslRepository).saveAs(Matchers.eq(driver), any());
  }

  @Test
  public void testHandleDriverUpdateSetsFinalReviewIfGotNoTerminalStatuses() {
    initListener(
      new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.FINAL_REVIEW), new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.NOT_CHANGED));
    Mockito.when(driverDslRepository.findById(Matchers.eq(DRIVER_ID))).thenReturn(driver);

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driver).setOnboardingStatus(DriverOnboardingStatus.FINAL_REVIEW);
  }

  @Test
  public void testHandleDriverUpdateSetsPendingIfGotAtLeastOnePending() {
    initListener(
      new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.FINAL_REVIEW),
      new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.NOT_CHANGED),
      new StubOnboardingStatusCheck(OnboardingStatusCheck.Result.PENDING));
    Mockito.when(driverDslRepository.findById(Matchers.eq(DRIVER_ID))).thenReturn(driver);

    testedInstance.handleDriverUpdate(new OnboardingUpdateEvent<>(new Driver(), new Driver(), DRIVER_ID));

    Mockito.verify(driver).setOnboardingStatus(DriverOnboardingStatus.PENDING);
  }

  private void initListener(OnboardingStatusCheck... checks) {
    ImmutableMap.Builder<String, OnboardingStatusCheck> builder = ImmutableMap.builder();
    for (OnboardingStatusCheck check : checks) {
      builder.put(check.toString(), check);
    }
    Mockito.when(applicationContext.getBeansOfType(OnboardingStatusCheck.class)).thenReturn(builder.build());
    testedInstance.onStartUp(new ContextRefreshedEvent(applicationContext));
    Mockito.when(driver.getOnboardingStatus()).thenReturn(null);
    driver.setCars(ImmutableSet.of(new Car()));
    when(documentDslRepository.findDocumentsByAvatarsAndTypes(anyCollection(), anyCollection()))
      .thenReturn(ImmutableMap.of(
        DocumentType.LICENSE, ImmutableMap.of(1L, new Document())
      ));
  }

  private static class StubOnboardingStatusCheck<T> implements OnboardingStatusCheck<T, OnboardingStatusCheck.Context> {
    private final OnboardingStatusCheck.Result result;

    private StubOnboardingStatusCheck(OnboardingStatusCheck.Result result) {
      this.result = result;
    }

    @Override
    public OnboardingStatusCheck.Result check(T old, T updated, Context context) {
      return result;
    }

    @Override
    public OnboardingStatusCheck.Result currentValue(T item, Context context) {
      return context.getCurrentCheckResult();
    }

    @Override
    public boolean supports(Class<?> clazz) {
      return true;
    }

  }

}