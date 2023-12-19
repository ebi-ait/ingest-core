package org.humancellatlas.ingest.dataset;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;

@Getter
@JsonIgnoreProperties({"firstDcpVersion", "dcpVersion", "validationState",
        "validationErrors", "graphValidationErrors", "isUpdate"})
public class Dataset extends MetadataDocument {
    @JsonCreator
    public Dataset(@JsonProperty("content") Object content) {
        super(EntityType.DATASET, content);
    }
}
