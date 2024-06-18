package org.humancellatlas.ingest.file.web;

import java.util.Optional;

import org.apache.http.entity.ContentType;
import org.humancellatlas.ingest.core.Checksums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by rolando on 07/09/2017. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileMessage {
  @JsonProperty("url")
  private String cloudUrl;

  @JsonProperty("name")
  private String fileName;

  @JsonProperty("upload_area_id")
  private String stagingAreaId;

  @JsonProperty("content_type")
  private String contentType;

  private Checksums checksums;
  private long size;

  /**
   * given existence of substring "dcp-type={type}" in this.contentType, extracts {type}
   *
   * @return the DCP media-type of the file uploaded that triggered this event
   */
  @JsonIgnore
  public Optional<String> getMediaType() {
    return Optional.ofNullable(ContentType.parse(this.getContentType()).getParameter("dcp-type"));
  }
}
