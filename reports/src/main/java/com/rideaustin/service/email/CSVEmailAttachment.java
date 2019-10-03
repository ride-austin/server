package com.rideaustin.service.email;

public class CSVEmailAttachment extends EmailAttachment {

  public CSVEmailAttachment(String name, String content) {
    super(name, content.getBytes(), ContentType.CSV);
  }
}
