package org.humancellatlas.ingest.file.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.core.Checksums;

/**
 * Created by rolando on 07/09/2017.
 */
@Getter
@AllArgsConstructor
public class FileMessage {
    @JsonProperty("url")
    private final String cloudUrl;
    @JsonProperty("name")
    private final String fileName;
    @JsonProperty("staging_area_id")
    private final String stagingAreaId;
    @JsonProperty("content_type")
    private final String contentType;
    private final Checksums checksums;
    private final long size;

}