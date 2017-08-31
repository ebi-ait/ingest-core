package org.humancellatlas.ingest.core;

import lombok.Data;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Data
public class Uuid {
    private final UUID uuid;

    protected Uuid(String name) {
        // throws IllegalArgumentException if not valid
        this.uuid = UUID.fromString(name);
    }

    public Uuid() {
        this.uuid = UUID.randomUUID();
    }
}
