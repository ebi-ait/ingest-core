package org.humancellatlas.ingest.schemas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;

@AllArgsConstructor
@Getter
public class Schema extends AbstractEntity implements Comparable<Schema> {

    private final String highLevelEntity;
    private final String schemaVersion;
    private final String domainEntity;
    private final String subDomainEntity;
    private final String concreteEntity;

    @JsonIgnore
    private final String schemaUri;

    @Override
    public int compareTo(Schema o) {
        return 0;
    }

}
