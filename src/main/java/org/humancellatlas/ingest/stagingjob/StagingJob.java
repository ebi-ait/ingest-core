package org.humancellatlas.ingest.stagingjob;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
import java.util.UUID;

@Getter
@CompoundIndexes({
        @CompoundIndex(name = "stagingAreaUuidAndFileName", def = "{'stagingAreaUuid' : 1, 'stagingAreaFileName' : 1}", unique = true)
})
@Document
@RequiredArgsConstructor
@EqualsAndHashCode
public class StagingJob implements Identifiable<String> {
    private @Id String id;
    private @CreatedDate Instant createdDate;

    private final UUID stagingAreaUuid;
    private final String stagingAreaFileName;

    private @Setter String stagingAreaFileUri;
}
