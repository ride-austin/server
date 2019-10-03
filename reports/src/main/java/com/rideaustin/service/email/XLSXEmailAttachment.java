package com.rideaustin.service.email;

public class XLSXEmailAttachment extends EmailAttachment {

  public XLSXEmailAttachment(String name, byte[] workbook) {
    super(name, workbook, ContentType.XLSX);
  }
}
