package uk.ac.ebi.subs.ingest.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Created by rolando on 06/09/2017. */
@Data
public class Checksums {
  private String sha1;
  private String sha256;
  private String crc32c;
  private String md5;

  @JsonProperty("s3_etag")
  private String s3Etag;

  public Checksums(String sha1, String sha256, String crc32c, String s3Etag) {
    this.sha1 = sha1;
    this.sha256 = sha256;
    this.crc32c = crc32c;
    this.s3Etag = s3Etag;
  }

  public Checksums(String md5) {
    this.md5 = md5;
  }

  protected Checksums() {}
}
