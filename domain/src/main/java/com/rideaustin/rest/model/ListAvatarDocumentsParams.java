package com.rideaustin.rest.model;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.QAvatarDocument;
import com.rideaustin.model.enums.DocumentType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
public class ListAvatarDocumentsParams {

  private static final QAvatarDocument qAvatarDocument = QAvatarDocument.avatarDocument;

  @Getter
  @Setter
  @ApiModelProperty(value = "Avatar ID", example = "1")
  private Long avatarId;
  @Getter
  @Setter
  @ApiModelProperty(value = "City ID", example = "1")
  private Long cityId;
  @Getter
  @Setter
  @ApiModelProperty(value = "Document type", allowableValues = "LICENSE,DRIVER_PHOTO,TNC_CARD,CHAUFFEUR_LICENSE")
  private DocumentType documentType;

  public void fill(BooleanBuilder builder) {
    if (avatarId != null) {
      builder.and(qAvatarDocument.avatar.id.eq(avatarId));
    }
    if (documentType != null) {
      builder.and(qAvatarDocument.document.documentType.eq(documentType));
    }
    if (cityId != null) {
      builder.and(qAvatarDocument.document.cityId.eq(cityId));
    }

  }
}
