package org.humancellatlas.ingest.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.*;

@Getter
@Setter
@AllArgsConstructor
public class File {
    private final Uuid uuid;
    private final String fileName;
    private final String cloudUrl;
    private final Checksums checksums;
}
