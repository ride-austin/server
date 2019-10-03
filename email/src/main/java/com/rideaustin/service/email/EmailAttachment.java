package com.rideaustin.service.email;

public abstract class EmailAttachment {

  private String name;
  private byte[] bytes;
  private ContentType contentType;

  public enum ContentType {
    CSV("text/csv;charset=UTF-8"),
    ZIP("application/zip"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private String mimeType;

    ContentType(String mimeType) {
      this.mimeType = mimeType;
    }

    @Override
    public String toString() {
      return mimeType;
    }

    public String getExtension() {
      return name().toLowerCase();
    }
  }

  protected EmailAttachment(String name, byte[] content, ContentType contentType) {
    this.name = String.format("%s.%s", name, contentType.getExtension());
    this.bytes = content;
    this.contentType = contentType;
  }

  public String getName() {
    return name;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public ContentType getContentType() {
    return contentType;
  }

}
