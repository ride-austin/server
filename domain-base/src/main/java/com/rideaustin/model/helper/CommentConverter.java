package com.rideaustin.model.helper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.rideaustin.utils.CryptUtils;

@Converter
public class CommentConverter implements AttributeConverter<String, String> {
  @Override
  public String convertToDatabaseColumn(String attribute) {
    return CryptUtils.encodeBase64(attribute);
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    return CryptUtils.base64Decode(dbData);
  }
}
