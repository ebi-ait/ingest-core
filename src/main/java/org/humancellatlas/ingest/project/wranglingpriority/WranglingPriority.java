package org.humancellatlas.ingest.project.wranglingpriority;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = WranglingPriorityDeserializer.class)
@JsonSerialize(using = WranglingPrioritySerializer.class)
public enum WranglingPriority {
    NOT_APPLICABLE("Not Applicable", 0),
    P1("1", 1),
    P2("2", 2);

    protected String text;
    protected int order;

    WranglingPriority(String text, Integer order) {
        this.text = text;
        this.order = order;
    }
}
