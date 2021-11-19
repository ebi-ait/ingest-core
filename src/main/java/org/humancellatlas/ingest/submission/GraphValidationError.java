// Temporary classes until dcp-507 is done

package org.humancellatlas.ingest.submission;
import lombok.Data;

@Data
class AffectedEntity {
    private String[] types;
    private String uuid;
}

@Data
public class GraphValidationError {
    private String test;
    private String message;
    private AffectedEntity[] affectedEntities;
}
 