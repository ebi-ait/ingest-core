package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = WranglingStateSerializer.class)
public enum WranglingState {
    NEW("New"),
    ELIGIBLE("Eligible"),
    NOT_ELIGIBLE("Not eligible"),
    IN_PROGRESS("In progress"),
    STALLED("Stalled"),
    SUBMITTED("Submitted"),
    PUBLISHED_IN_DCP("Published in DCP"),
    DELETED("Deleted"),
    NEW_SUGGESTION("New Suggestion");

    protected String text;

    WranglingState(String status) {
        this.text = status;
    }

    @JsonValue
    public String getValue() {
        return this.text;
    }
}
