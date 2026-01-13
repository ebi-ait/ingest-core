package uk.ac.ebi.subs.ingest.dataset.util;

import lombok.Value;

@Value
public class CommandStatusResponse {
    String status;     // e.g. Success | Failed | InProgress
    String globusTaskId;
    String stdout;
    String stderr;
}
