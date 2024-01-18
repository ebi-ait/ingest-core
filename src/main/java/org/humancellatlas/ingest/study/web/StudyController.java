package org.humancellatlas.ingest.study.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.study.Study;
import org.humancellatlas.ingest.study.StudyRepository;
import org.humancellatlas.ingest.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RepositoryRestController
@ExposesResourceFor(Study.class)
@RequiredArgsConstructor
@Getter
public class StudyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyController.class);

    private final @NonNull StudyService studyService;

    private final @NonNull StudyRepository studyRepository;

    private final Environment environment;


    @PostMapping("/studies")
    public ResponseEntity<Resource<?>> registerStudy(@RequestBody final Study study,
                                                     final PersistentEntityResourceAssembler assembler) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("morphic")) {
            Study result = studyService.register(study);
            return ResponseEntity.ok().body(assembler.toFullResource(result));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/studies/{studyId}")
    public ResponseEntity<Resource<?>> updateStudy(@PathVariable String studyId,
                                                   @RequestBody final ObjectNode patch,
                                                   final PersistentEntityResourceAssembler assembler) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("morphic")) {
            Study updatedStudy = studyService.update(studyId, patch);
            return ResponseEntity.ok().body(assembler.toFullResource(updatedStudy));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/studies/{studyId}")
    public ResponseEntity<Resource<?>> replaceStudy(@PathVariable String studyId,
                                                    @RequestBody final Study updatedStudy,
                                                    final PersistentEntityResourceAssembler assembler) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("morphic")) {
            Study replacedStudy = studyService.replace(studyId, updatedStudy);
            return ResponseEntity.ok().body(assembler.toFullResource(replacedStudy));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/studies/{studyId}")
    public ResponseEntity<Void> deleteStudy(@PathVariable String studyId) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("morphic")) {
            studyService.delete(studyId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}