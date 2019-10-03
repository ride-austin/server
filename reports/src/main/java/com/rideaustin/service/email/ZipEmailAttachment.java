package com.rideaustin.service.email;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public class ZipEmailAttachment extends EmailAttachment {

  public ZipEmailAttachment(String name, File zipFile) throws IOException {
    super(name, Files.toByteArray(zipFile), ContentType.ZIP);
  }
}
