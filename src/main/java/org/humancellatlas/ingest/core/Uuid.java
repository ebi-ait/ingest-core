package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Data
@EqualsAndHashCode
public class Uuid {
    private UUID uuid;

    @JsonCreator
    public Uuid(String name) {
        // throws IllegalArgumentException if not valid
        this.uuid = UUID.fromString(name);
    }

    public Uuid() {
    }

    public static Uuid newUuid() {
        Uuid uuid = new Uuid();
        uuid.setUuid(UUID.randomUUID());
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

}
