package org.humancellatlas.ingest.bundle;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EntityLink {
    private List<String> outputs;

    private String process;

    private String output_type;

    private List<String> inputs;

    private String input_type;

    private List<Map<String, String>> protocols;
}
