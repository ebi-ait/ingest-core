package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.process.Process;

@Getter
@EqualsAndHashCode(callSuper = true)
public class Protocol extends MetadataDocument {

    private boolean linked = false;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Protocol(Object content) {
        super(EntityType.PROTOCOL, content);
    }

    public boolean isLinked() {
        return linked;
    }

    public void useFor(Process process) {
        this.linked = true;
    }

}
