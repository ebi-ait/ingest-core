package org.humancellatlas.ingest.submission.manifest;

import org.humancellatlas.ingest.core.AbstractEntity;

import java.util.Collections;
import java.util.Map;

/**
 * Created by rolando on 30/05/2018.
 */
public class EmptyManifest extends AbstractEntity implements SubmissionManifest {
    private static final Map empty = Collections.unmodifiableMap(Collections.EMPTY_MAP);
}
