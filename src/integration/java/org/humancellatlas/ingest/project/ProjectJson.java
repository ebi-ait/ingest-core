package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class ProjectJson {
    String title;

    public static ProjectJson fromTitle(String title){
        ProjectJson project = new ProjectJson();
        project.title = title;
        return project;
    }

    public ObjectNode toObjectNode() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode content = mapper.createObjectNode();
        ObjectNode projectCore0 = content.putObject("project_core");
        projectCore0.put("project_title", this.title);

        ObjectNode metadata = mapper.createObjectNode();
        metadata.set("content", content);
        return metadata;
    }

    public  Map<String, Object> toMap() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode project = this.toObjectNode();
        return mapper.convertValue(project, new TypeReference<Map<String, Object>>(){});
    }
}
