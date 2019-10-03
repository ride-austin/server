package com.rideaustin.service.thirdparty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.rideaustin.rest.exception.ServerError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!itest")
public class S3StorageService {

  private static final int PRESIGNED_URL_EXPIRATION_TIME = 1000 * 60 * 3;

  private final AmazonS3 s3;

  private final String s3BucketName;
  private final String s3Protocol;

  private static final String PNG_MIME_TYPE = "image/png";

  private MimeTypes mimeTypes;

  @Inject
  public S3StorageService(Environment env) {
    s3 = new AmazonS3Client(new BasicAWSCredentials(
      env.getProperty("ra.aws.key.access"),
      env.getProperty("ra.aws.key.secret")));
    s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());

    s3BucketName = env.getProperty("ra.s3.bucket");
    s3Protocol = env.getProperty("ra.s3.protocol", "https");
    mimeTypes = MimeTypes.getDefaultMimeTypes();
  }

  public byte[] loadFile(String fileKey) throws IOException {
    GetObjectRequest request = new GetObjectRequest(s3BucketName, fileKey);
    S3Object object = s3.getObject(request);
    return IOUtils.toByteArray(object.getObjectContent());
  }

  public byte[] loadCachedFile(String fileKey, Date ifModifiedSince) throws IOException {
    GetObjectRequest request = new GetObjectRequest(s3BucketName, fileKey);
    request.setModifiedSinceConstraint(ifModifiedSince);
    S3Object object = s3.getObject(request);
    if (object == null) {
      return new byte[0];
    }
    return IOUtils.toByteArray(object.getObjectContent());
  }

  @Nonnull
  public String uploadStorageItem(StorageItem storageItem) {
    if (storageItem.getFileName() == null) {
      String uuid = UUID.randomUUID().toString();
      storageItem.setFileName(uuid);
      try {
        storageItem.setFileName(uuid + mimeTypes.forName(storageItem.getMimeType()).getExtension());
      } catch (MimeTypeException e) {
        log.error("Unknown mime type " + storageItem.getMimeType(), e);
      }
    }

    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentType(storageItem.getMimeType());
    meta.setContentLength(storageItem.getContent().length);

    if (storageItem.getExpirationDate() != null) {
      meta.setExpirationTime(storageItem.getExpirationDate());
    }

    String fileKey = storageItem.getFileName();
    if (storageItem.getFolder() != null) {
      fileKey = storageItem.getFolder() + "/" + fileKey;
    }
    PutObjectRequest request = new PutObjectRequest(s3BucketName, fileKey,
      new ByteArrayInputStream(storageItem.getContent()), meta);
    if (storageItem.isPublicAccess()) {
      AccessControlList acl = new AccessControlList();
      acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
      request.withAccessControlList(acl);
    }
    s3.putObject(request);

    return fileKey;
  }

  @Nonnull
  protected String uploadFile(@Nonnull String folder, @Nonnull InputStream input, long length, boolean publicAccess) {
    String uuid = UUID.randomUUID().toString();

    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentType(PNG_MIME_TYPE);
    meta.setContentLength(length);

    String fileKey = folder + "/" + uuid + ".png";
    PutObjectRequest request = new PutObjectRequest(s3BucketName, fileKey, input, meta);
    if (publicAccess) {
      AccessControlList acl = new AccessControlList();
      acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
      request.withAccessControlList(acl);
    }
    s3.putObject(request);

    return fileKey;
  }

  @Nonnull
  public String uploadPrivateFile(@Nonnull String folder, @Nonnull byte[] bytes) {
    return uploadFile(folder, new ByteArrayInputStream(bytes), bytes.length, false);
  }

  @Nonnull
  public String savePrivateOrThrow(@Nonnull String folder, @Nonnull MultipartFile file) throws ServerError {
    try {
      return uploadFile(folder, file.getInputStream(), file.getSize(), false);
    } catch (IOException e) {
      throw new ServerError(e);
    }
  }

  @Nonnull
  private String uploadPublicFile(@Nonnull String folder, @Nonnull InputStream input, long length) {
    String fileKey = uploadFile(folder, input, length, true);
    return getUnsignedURL(fileKey);
  }

  @Nullable
  public String uploadPublicFile(@Nonnull String folder, @Nullable String data) {
    if (data == null) {
      return null;
    }
    byte[] bytes = DatatypeConverter.parseBase64Binary(data);
    return uploadPublicFile(folder, new ByteArrayInputStream(bytes), bytes.length);
  }

  public String savePublicOrThrow(@Nonnull String type, @Nonnull String folder, @Nonnull MultipartFile file) throws ServerError {
    try {
      String url = uploadPublicFile(folder, file.getInputStream(), file.getSize());
      log.debug("Saving {}:{}", type, url);
      return url;
    } catch (IOException e) {
      throw new ServerError(e);
    }
  }

  @Nonnull
  public String getSignedURL(@Nonnull String key) {
    log.debug(String.format("Generating presigned url for :%s", key));
    GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(s3BucketName, key);
    Date urlExpiration = new Date();
    long currentTime = urlExpiration.getTime();
    urlExpiration.setTime(currentTime + PRESIGNED_URL_EXPIRATION_TIME);
    presignedUrlRequest.setExpiration(urlExpiration);
    String url = s3.generatePresignedUrl(presignedUrlRequest).toString();
    log.debug(String.format("Generated URL is:%s", url));
    return url;
  }

  public String getUnsignedURL(String fileKey) {
    return s3Protocol + "://" + s3BucketName + "/" + fileKey;
  }

}
