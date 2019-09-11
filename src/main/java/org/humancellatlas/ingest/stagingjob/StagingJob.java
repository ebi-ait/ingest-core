package org.humancellatlas.ingest.stagingjob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
import java.util.UUID;

@Getter
@CompoundIndexes({
        @CompoundIndex(
                name = "stagingAreaUuidAndFileName",
                def = "{'stagingAreaUuid' : 1, 'stagingAreaFileName' : 1}",
                unique = true
        )
})
@Document
@EqualsAndHashCode
@RequiredArgsConstructor
public class StagingJob implements Identifiable<String> {

    @Id
    private String id;

    @CreatedDate
    private Instant createdDate;

    @Indexed
    private final UUID stagingAreaUuid;

    private final String stagingAreaFileName;

    private String metadataUuid;

    @Setter
    private String stagingAreaFileUri;

    @JsonCreator
    @PersistenceConstructor
    public StagingJob(@JsonProperty(value = "stagingAreaUuid") UUID stagingAreaUuid,
            @JsonProperty(value = "metadataUuid") String metadataUuid,
            @JsonProperty(value = "stagingAreaFileName") String stagingAreaFileName) {
        this.stagingAreaUuid = stagingAreaUuid;
        this.metadataUuid = metadataUuid;
        this.stagingAreaFileName = stagingAreaFileName;
    }

}
