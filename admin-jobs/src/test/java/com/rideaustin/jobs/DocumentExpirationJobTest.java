package com.rideaustin.jobs;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.service.DocumentService;

public class DocumentExpirationJobTest {

  @InjectMocks
  private DocumentExpirationJob testedInstance;
  @Mock
  private DocumentService documentService;
  @Mock
  private DocumentDslRepository documentDslRepository;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecuteInternal() throws Exception {
    when(documentDslRepository.findDocumentsIdsToExpireToday()).thenReturn(ImmutableList.of(1L));
    testedInstance.executeInternal();
    verify(documentService).updatedExpiredDocument(anyLong());
  }

}