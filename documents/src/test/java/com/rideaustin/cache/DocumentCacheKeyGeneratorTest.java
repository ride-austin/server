package com.rideaustin.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.cache.DocumentCacheKeyGenerator.DocumentKey;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;

public class DocumentCacheKeyGeneratorTest {

  private DocumentCacheKeyGenerator testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DocumentCacheKeyGenerator();
  }

  @Test
  public void testGenerateKeyForCar() {
    final long id = 1L;
    Car subject = new Car();
    subject.setId(id);

    final DocumentKey key = (DocumentKey) testedInstance.generate(null, null, subject);

    assertNull(key.getType());
    assertEquals(id, key.getOwnerId());
  }

  @Test
  public void testGenerateKeyForAvatar() {
    final long id = 1L;
    Driver subject = new Driver();
    subject.setId(id);

    final DocumentKey key = (DocumentKey) testedInstance.generate(null, null, subject);

    assertNull(key.getType());
    assertEquals(id, key.getOwnerId());
  }

  @Test
  public void testGenerateKeyForDocument() {
    final long id = 1L;
    Driver subject = new Driver();
    subject.setId(id);
    final DocumentType documentType = DocumentType.DRIVER_PHOTO;
    Document document = new Document();
    document.setId(2L);
    document.setDocumentType(documentType);

    final DocumentKey key = (DocumentKey) testedInstance.generate(null, null, subject, document);

    assertEquals(documentType, key.getType());
    assertEquals(id, key.getOwnerId());
  }

  @Test
  public void testGenerateForDocumentType() {
    final long id = 1L;
    Driver subject = new Driver();
    subject.setId(id);
    final DocumentType documentType = DocumentType.DRIVER_PHOTO;

    final DocumentKey key = (DocumentKey) testedInstance.generate(null, null, subject, documentType);

    assertEquals(documentType, key.getType());
    assertEquals(id, key.getOwnerId());
  }

  @Test
  public void testGenerateForId() {
    final long id = 1L;

    final DocumentKey key = (DocumentKey) testedInstance.generate(null, null, id);

    assertNull(key.getType());
    assertEquals(id, key.getOwnerId());
  }
}