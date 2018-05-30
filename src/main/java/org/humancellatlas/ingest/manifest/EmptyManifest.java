package org.humancellatlas.ingest.manifest;

import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Collections;
import java.util.Map;

/**
 * Created by rolando on 30/05/2018.
 */
@AllArgsConstructor
public class EmptyManifest extends AbstractEntity implements SubmissionManifest {
    private static final Map empty = Collections.unmodifiableMap(Collections.EMPTY_MAP);

    private final @DBRef SubmissionEnvelope submissionEnvelope;
}
