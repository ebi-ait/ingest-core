package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
@EqualsAndHashCode(callSuper = true, exclude = {"supplementaryFiles", "submissionEnvelopes"})
public class Project extends MetadataDocument {
    @RestResource
    @JsonIgnore
    @DBRef(lazy = true)
    private Set<File> supplementaryFiles = new HashSet<>();

    // A project may have 1 or more submissions related to it.
    @JsonIgnore
    private @DBRef(lazy = true)
    Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

    @Setter
    private Instant releaseDate;

    @Setter
    private Instant accessionDate;

    @Setter
    private Object technology;

    @Setter
    private Object organ;

    @Setter
    private Integer cellCount;

    @Setter
    private Object dataAccess;

    @Setter
    private Object identifyingOrganisms;

    @Setter
    private String primaryWrangler;

    @Setter
    private String secondaryWrangler;

    @Setter
    private WranglingState wranglingState;

    @Setter
    private Integer wranglingPriority;

    @Setter
    private String wranglingNotes;

    @Setter
    private Boolean isInCatalogue;

    @Setter
    private Instant cataloguedDate;

    @Setter
    private List<Object> publicationsInfo;

    @Setter
    private Integer dcpReleaseNumber;

    @Setter
    private List<String> projectLabels;

    @Setter
    private List<String> projectNetworks;


    @JsonCreator
    public Project(@JsonProperty("content") Object content) {
        super(EntityType.PROJECT, content);
    }

    public void addToSubmissionEnvelopes(@NotNull SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelopes.add(submissionEnvelope);
    }

    //ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
    @JsonIgnore
    public List<SubmissionEnvelope> getOpenSubmissionEnvelopes() {
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .filter(env -> env.getSubmissionState() != null)
                .filter(SubmissionEnvelope::isOpen)
                .collect(Collectors.toList());
    }

    public Boolean getHasOpenSubmission() {
        return !getOpenSubmissionEnvelopes().isEmpty();
    }

    @JsonIgnore
    public Boolean isEditable() {
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .allMatch(SubmissionEnvelope::isEditable);
    }

    public static ProjectBuilder builder() {
        return new ProjectBuilder();
    }

    /**
     * generic builder that uses reflection.
     * Might not be suitable for production due to slower performance.
     * Currently used only for testing.
     */
    public static class ProjectBuilder {
        Map<String, String> dataAccess;
        Map<String, Map> content = new HashMap<String, Map>();

        Uuid uuid = Uuid.newUuid();

        public ProjectBuilder emptyProject() {
            return this;
        }

        public ProjectBuilder withManagedAccess() {
            dataAccess = Map.of("type", DataAccessTypes.OPEN.getLabel());
            return this;
        }

        public ProjectBuilder withOpenAccess() {
            dataAccess = Map.of("type", DataAccessTypes.OPEN.getLabel());
            return this;
        }

        public ProjectBuilder withUuid(String uuid) {
            this.uuid = new Uuid(uuid);
            return this;
        }

        public ProjectBuilder withShortName(String shortName) {
            Map<String, Object> projectCore =
                    content.computeIfAbsent("project_core", k -> new HashMap<String, Object>());
            projectCore.put("project_short_name", shortName);
            return this;
        }

        public Project build() {
            Project project = new Project(content);
            copyFieldsFromBuilder(project);
            return project;
        }

        private void copyFieldsFromBuilder(Project project) {
            List<String> constructorFields = List.of("content");
            Map<String, Field> fieldsMap = Stream.iterate((Class) project.getClass(),
                            c -> c.getSuperclass() != null,
                            c -> c.getSuperclass())
                    .map(Class::getDeclaredFields)
                    .flatMap(Arrays::stream)
                    .filter(f->!constructorFields.contains(f.getName()))
                    .collect(Collectors.toMap(Field::getName, Function.identity()));
            Arrays.stream(ProjectBuilder.class.getDeclaredFields())
                    .filter(f->!constructorFields.contains(f.getName()))
                    .forEach(builderField -> {
                        try {
                            Field projectField = fieldsMap.get(builderField.getName());
                            String fieldName = projectField.getName();
                            Method setter = setterForField(project, fieldName, projectField);
                            setter.invoke(project, builderField.get(this));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        private static Method setterForField(Project project, String fieldName, Field projectField) throws NoSuchMethodException {
            return project.getClass().getMethod(toSetterName(fieldName), projectField.getType());
        }

        private static String toSetterName(String fieldName) {
            return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }

        public Map<String, Object> asMap() {
            ObjectMapper objectMapper = new ObjectMapper();
            Project project = this.build();
            Map<String, Object> projectAsMap = objectMapper.convertValue(project,
                    new TypeReference<Map<String, Object>>() {
                    });
            // TODO amnon: needed because of serialization problem. Not sure why.
            projectAsMap.remove("contentLastModified");
            return projectAsMap;
        }
    }
}
