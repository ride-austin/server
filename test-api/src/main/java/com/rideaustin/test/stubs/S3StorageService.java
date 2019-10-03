package com.rideaustin.test.stubs;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.core.env.Environment;

import com.rideaustin.utils.RandomString;

public class S3StorageService extends com.rideaustin.service.thirdparty.S3StorageService {

  @Inject
  public S3StorageService(Environment env) {
    super(env);
  }

  @Nonnull
  @Override
  protected String uploadFile(@Nonnull String folder, @Nonnull InputStream input, long length, boolean publicAccess) {
    return RandomString.generate(20);
  }

  @Override
  public byte[] loadFile(String fileKey) {
    return new byte[0];
  }
}
