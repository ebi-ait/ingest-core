package org.humancellatlas.ingest.schemas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by rolando on 18/04/2018.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Schema {
    @JsonProperty("high-level-entity")
    private String highLevelEntity;
    @JsonProperty("version")
    private String version;
    @JsonProperty("domain_entity")
    private String domainEntity;
    @JsonProperty("sub_domain_entity")
    private String subDomainEntity;
    @JsonProperty("concrete_entity")
    private String concreteEntity;

    @JsonIgnore
    private String schemaUri;
}
