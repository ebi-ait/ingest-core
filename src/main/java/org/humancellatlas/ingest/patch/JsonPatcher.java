package org.humancellatlas.ingest.patch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class JsonPatcher {

    private final DomainObjectReader domainObjectReader;

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonPatcher(PersistentEntities persistentEntities, Associations associations, ObjectMapper objectMapper) {
        this.domainObjectReader = new DomainObjectReader(persistentEntities, associations);
        this.objectMapper = objectMapper;
    }

    public <T> T merge(InputStream patch, T target) {
        return domainObjectReader.read(patch, target, objectMapper);
    }

}
