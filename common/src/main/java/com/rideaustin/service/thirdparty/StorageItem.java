package com.rideaustin.service.thirdparty;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageItem {

  private static final String TEXT_PLAIN = "text/plain";
  private static final String ATTACHMENTS_FOLDER = "attachments";

  private String folder;
  private byte[] content;
  private boolean publicAccess;
  private String mimeType;
  private Date expirationDate;
  private String fileName;

  public StorageItem() {
    setMimeType(TEXT_PLAIN);
    setFolder(ATTACHMENTS_FOLDER);
    publicAccess = false;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private StorageItem item;

    public Builder() {
      this.item = new StorageItem();
    }

    public Builder setExpirationHours(int hours) {
      item.setExpirationDate(DateUtils.addHours(new Date(), hours));
      return this;
    }

    public Builder setFolder(String folder) {
      item.setFolder(folder);
      return this;
    }

    public Builder setName(String name) {
      item.setFileName(name);
      return this;
    }

    public Builder setMimeType(String mimeType) {
      item.setMimeType(mimeType);
      return this;
    }

    public Builder setPublicAccess(boolean access) {
      item.setPublicAccess(access);
      return this;
    }

    public Builder setContent(byte[] content) {
      item.setContent(content);
      return this;
    }

    public StorageItem build() {
      return item;
    }
  }
}
