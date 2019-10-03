package com.rideaustin.test.fixtures;

import com.rideaustin.model.AvatarDocument;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.Avatar;

public class AvatarDocumentFixture extends AbstractFixture<AvatarDocument> {

  private Avatar avatar;
  private DocumentType documentType;
  private String documentUrl;

  public AvatarDocumentFixture(DocumentType documentType, String documentUrl) {
    this.documentType = documentType;
    this.documentUrl = documentUrl;
  }

  @Override
  protected AvatarDocument createObject() {
    return AvatarDocument.builder()
      .document(Document.builder()
        .documentStatus(DocumentStatus.APPROVED)
        .documentUrl(documentUrl)
        .documentType(documentType)
        .removed(false)
        .build()
      )
      .avatar(avatar)
      .build();
  }

  @Override
  public AvatarDocument getFixture() {
    AvatarDocument avatarDocument = createObject();
    avatarDocument.setDocument(entityManager.merge(avatarDocument.getDocument()));
    return entityManager.merge(avatarDocument);
  }

  public void setAvatar(Avatar avatar) {
    this.avatar = avatar;
  }

  public Avatar getAvatar() {
    return avatar;
  }
}
