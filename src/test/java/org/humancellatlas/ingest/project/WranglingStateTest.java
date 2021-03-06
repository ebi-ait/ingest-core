package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.project.WranglingState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

@JsonTest
public class WranglingStateTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDeserialize() throws IOException {
        assertEquals(objectMapper.readValue("\"New\"", WranglingState.class), WranglingState.NEW);
        assertEquals(objectMapper.readValue("\"Eligible\"", WranglingState.class), WranglingState.ELIGIBLE);
        assertEquals(objectMapper.readValue("\"Not eligible\"", WranglingState.class), WranglingState.NOT_ELIGIBLE);
        assertEquals(objectMapper.readValue("\"In progress\"", WranglingState.class), WranglingState.IN_PROGRESS);
        assertEquals(objectMapper.readValue("\"Stalled\"", WranglingState.class), WranglingState.STALLED);
        assertEquals(objectMapper.readValue("\"Submitted\"", WranglingState.class), WranglingState.SUBMITTED);
        assertEquals(objectMapper.readValue("\"Published in DCP\"", WranglingState.class), WranglingState.PUBLISHED_IN_DCP);
        assertEquals(objectMapper.readValue("\"Deleted\"", WranglingState.class), WranglingState.DELETED);
    }

    @Test
    public void testSerialize() throws JsonProcessingException {
        assertEquals(objectMapper.writeValueAsString(WranglingState.NEW),"\"New\"");
        assertEquals(objectMapper.writeValueAsString(WranglingState.ELIGIBLE),"\"Eligible\"");
        assertEquals(objectMapper.writeValueAsString(WranglingState.NOT_ELIGIBLE),"\"Not eligible\"");
        assertEquals(objectMapper.writeValueAsString(WranglingState.IN_PROGRESS),"\"In progress\"");
        assertEquals(objectMapper.writeValueAsString(WranglingState.STALLED),"\"Stalled\"");
        assertEquals(objectMapper.writeValueAsString(WranglingState.PUBLISHED_IN_DCP),"\"Published in DCP\"");
        assertEquals(objectMapper.writeValueAsString(WranglingState.DELETED),"\"Deleted\"");
    }
}
