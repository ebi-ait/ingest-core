package org.humancellatlas.ingest.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

@JsonTest
public class AuditTypeTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDeserialize() throws IOException {
        assertEquals(objectMapper.readValue("\"Status updated\"", AuditType.class), AuditType.STATUS_UPDATED);
    }

    @Test
    public void testSerialize() throws JsonProcessingException {
        assertEquals(objectMapper.writeValueAsString(AuditType.STATUS_UPDATED),"\"Status updated\"");
    }
}
