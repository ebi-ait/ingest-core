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

    /* TODO
    This method was originally made as simple as possible to only support the orphaned entity use case. However,
    this can be enhanced further to add a full-fledged component that enables back linking to all Processes that refer
    to this Protocol. In that case, the isLinked implementation will need to be changed to check if the list of
    processes is empty or not. This approach was not initially chosen because it would have required data migration.
     */
    public void useFor(Process process) {
        this.linked = true;
    }

}
