package org.humancellatlas.ingest.project.wranglingstate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = WranglingStateSerializer.class)
@JsonDeserialize(using = WranglingStateDeserializer.class)
public enum WranglingState {
    NEW("New"),
    ELIGIBLE("Eligible"),
    NOT_ELIGIBLE("Not eligible"),
    IN_PROGRESS("In progress"),
    STALLED("Stalled"),
    SUBMITTED("Submitted"),
    PUBLISHED_IN_DCP("Published in DCP"),
    DELETED("Deleted");

    protected String text;

    WranglingState(String status) {
        this.text = status;
    }
}
