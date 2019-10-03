package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class ChauffeurLicenseExpirationHandlerTest {

  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private DriverTypeCache driverTypeCache;

  private ChauffeurLicenseExpirationHandler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DriverTypeUtils.setDriverTypeCache(driverTypeCache);

    testedInstance = new ChauffeurLicenseExpirationHandler(documentDslRepository);
  }

  @DataProvider
  public static Object[] ineligibleTypes() {
    return EnumSet.complementOf(EnumSet.of(DocumentType.CHAUFFEUR_LICENSE)).toArray();
  }

  @Test
  public void supportsReturnsTrueOnChauffeurLicense() {
    final boolean result = testedInstance.supports(DocumentType.CHAUFFEUR_LICENSE);

    assertTrue(result);
  }

  @Test
  public void handleSkipsOrphanDocuments() {
    when(documentDslRepository.findDriver(any(Document.class))).thenReturn(null);

    testedInstance.handle(new Document());

    verify(documentDslRepository, never()).saveAny(any(Driver.class));
  }

  @Test
  public void handleUpdatesDriverTypeBitmask() {
    final Driver driver = new Driver();
    driver.setGrantedDriverTypesBitmask(2);
    when(documentDslRepository.findDriver(any(Document.class))).thenReturn(driver);
    when(driverTypeCache.toBitMask(anySetOf(String.class))).thenReturn(2);

    testedInstance.handle(new Document());

    assertEquals(0, driver.getGrantedDriverTypesBitmask().intValue());
    verify(documentDslRepository, times(1)).saveAny(eq(driver));
  }

  @Test
  @UseDataProvider("ineligibleTypes")
  public void supportsReturnsFalseOnOtherDocTypes(DocumentType type) {
    final boolean result = testedInstance.supports(type);

    assertFalse(result);
  }
}