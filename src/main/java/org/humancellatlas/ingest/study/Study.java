package org.humancellatlas.ingest.study;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;

@Getter
@JsonIgnoreProperties({"firstDcpVersion", "dcpVersion", "validationState",
        "validationErrors", "graphValidationErrors", "isUpdate"})
public class Study extends MetadataDocument {

    @JsonCreator
    public Study(@JsonProperty("content") Object content) {
        super(EntityType.STUDY, content);
    }

}
