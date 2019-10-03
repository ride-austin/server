package com.rideaustin.service.onboarding;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;

public class DocumentOnboardingStatusCheckTest extends BaseOnboardingStatusCheckTest<Document> {
  @Test
  public void testCheckSetsPending() {
    List<Triple<Document, Document, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDocument(DocumentStatus.APPROVED), createDocument(DocumentStatus.PENDING), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDocument(DocumentStatus.APPROVED), createDocument(DocumentStatus.EXPIRED), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDocument(DocumentStatus.APPROVED), createDocument(DocumentStatus.REJECTED), OnboardingStatusCheck.Result.PENDING)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void testCheckSetsFinalReview() {
    List<Triple<Document, Document, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDocument(DocumentStatus.PENDING), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDocument(DocumentStatus.EXPIRED), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDocument(DocumentStatus.REJECTED), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW)
      );

    assertResultOnUpdate(data);
  }

  @Test
  public void shouldReturnNotChangedOnTerminalStateWithActiveOnboarding() {
    List<Triple<Document, Document, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDocument(DocumentStatus.PENDING), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDocument(DocumentStatus.EXPIRED), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDocument(DocumentStatus.REJECTED), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED)
      );

    doTest(data, new OnboardingStatusCheck.Context(DriverOnboardingStatus.ACTIVE, null));
  }

  @Test
  public void testCheckSetsNotChanged() {
    List<Triple<Document, Document, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDocument(DocumentStatus.PENDING), createDocument(DocumentStatus.PENDING), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDocument(DocumentStatus.EXPIRED), createDocument(DocumentStatus.EXPIRED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDocument(DocumentStatus.REJECTED), createDocument(DocumentStatus.REJECTED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDocument(DocumentStatus.APPROVED), createDocument(DocumentStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    assertResultOnUpdate(data);
  }

  @Override
  protected OnboardingStatusCheck<Document, OnboardingStatusCheck.Context> getCheck() {
    return new DocumentOnboardingStatusCheck();
  }
}