package org.humancellatlas.ingest.file.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.entity.ContentType;
import org.humancellatlas.ingest.core.Checksums;
import org.springframework.util.StringUtils;

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
    @JsonProperty("upload_area_id")
    private final String stagingAreaId;
    @JsonProperty("content_type")
    private final String contentType;
    private final Checksums checksums;
    private final long size;

    /**
     * given existence of substring "dcp-type={type}" in this.contentType, extracts {type}
     *
     * @return the DCP media-type of the file uploaded that triggered this event
     */
    @JsonIgnore
    public String getMediaType(){
        return ContentType.parse(this.getContentType()).getParameter("dcp-type");
    }
}