package com.rideaustin.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CryptUtils {

  @Value("${app.secret}")
  private String appSecret;

  /**
   * Computes MD5 hash of {@code username.toLowerCase() + secret + password}
   *
   * @param username
   * @param password
   * @return MD5 hash as hexadecimal string
   */
  public String clientAppHash(String username, String password) {
    return DigestUtils.md5Hex(username.toLowerCase() + appSecret + password);
  }

  public static String encodeBase64(String textToEncode) {
    if (StringUtils.isNotEmpty(textToEncode)) {
      return DatatypeConverter.printBase64Binary(textToEncode.getBytes(UTF_8));
    } else {
      return textToEncode;
    }
  }

  public static String base64Decode(String base64Text) {
    if (StringUtils.isNotEmpty(base64Text)) {
      return new String(DatatypeConverter.parseBase64Binary(base64Text), UTF_8);
    } else {
      return base64Text;
    }
  }

}
