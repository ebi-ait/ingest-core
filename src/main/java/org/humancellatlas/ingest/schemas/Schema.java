package org.humancellatlas.ingest.schemas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;

/**
 * Created by rolando on 18/04/2018.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Schema extends AbstractEntity {
    private String highLevelEntity;
    private String schemaVersion;
    private String domainEntity;
    private String subDomainEntity;
    private String concreteEntity;

    @JsonIgnore
    private String schemaUri;
}
