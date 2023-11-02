package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.core.Uuid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * generic builder that uses reflection.
 * Might not be suitable for production due to slower performance.
 * Currently used only for testing.
 */
public class ProjectBuilder {
    public final BuilderHelper builderHelper = new BuilderHelper<Project, ProjectBuilder>(this);
    Map<String, Object> content = new HashMap<>();

    Uuid uuid = Uuid.newUuid();

    public ProjectBuilder emptyProject() {
        return this;
    }

    public ProjectBuilder withManagedAccess() {
        return withDataAccess(new DataAccess(DataAccessTypes.MANAGED));
    }

    public ProjectBuilder withOpenAccess() {
        return withDataAccess(new DataAccess(DataAccessTypes.OPEN));
    }

    public ProjectBuilder withDataAccess(DataAccess dataAccess) {
        content.put("dataAccess", dataAccess);
        return this;
    }

    public ProjectBuilder withUuid(String uuid) {
        this.uuid = new Uuid(uuid);
        return this;
    }

    public ProjectBuilder withShortName(String shortName) {
        Map<String, Object> projectCore =
                (Map<String, Object>) content.computeIfAbsent("project_core", k -> new HashMap<String, Object>());
        projectCore.put("project_short_name", shortName);
        return this;
    }

    public Project build() {
        Project project = new Project(content);
        List<String> constructorFields = List.of("content");
        builderHelper.copyFieldsFromBuilder(project, constructorFields);
        return project;
    }

    public Map<String, Object> asMap() {
        return builderHelper.asMap(List.of("contentLastModified"));
    }

}
