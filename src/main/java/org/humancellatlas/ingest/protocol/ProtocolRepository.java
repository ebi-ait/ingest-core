package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.protocol.Protocol;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface ProtocolRepository extends MongoRepository<Protocol, String> {
    public Protocol findByUuid(UUID uuid);
}
