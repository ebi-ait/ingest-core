package org.humancellatlas.ingest.schemas;

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
    @JsonProperty("domain_entity")
    private String domainEntity;
    @JsonProperty("concrete_entity")
    private String concreteEntity;
    @JsonProperty("version")
    private String version;
    @JsonProperty("_links")
    private JsonNode links;
}
