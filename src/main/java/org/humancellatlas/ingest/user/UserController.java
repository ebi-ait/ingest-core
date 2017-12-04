package org.humancellatlas.ingest.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    private PagedResourcesAssembler<SubmissionEnvelope> submissionEnvelopePagedResourcesAssembler;

    @Autowired
    private PagedResourcesAssembler<Project> projectPagedResourcesAssembler;

    @RequestMapping(value = "/summary")
    @ResponseBody
    public Summary summary() {
        String user = getPrincipal();

        long pendingSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.PENDING, user);
        long draftSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.DRAFT, user);
        long validatingubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.VALIDATING, user);
        long validSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.VALID, user);
        long invalidSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.INVALID, user);
        long submittedSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.SUBMITTED, user);
        long processingSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.PROCESSING, user);
        long cleanupSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.CLEANUP, user);
        long completedSubmissions = submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.COMPLETE, user);
        long projects = projectRepository.countByUser(user);
        return new Summary(pendingSubmissions, draftSubmissions, completedSubmissions, projects);
    }

    @RequestMapping(value = "/submissionEnvelopes")
    public PagedResources<Resource<SubmissionEnvelope>> getUserSubmissionEnvelopes(Pageable pageable) {
        Page<SubmissionEnvelope> submissionEnvelopes = submissionEnvelopeRepository.findByUser(getPrincipal(), pageable);
        return submissionEnvelopePagedResourcesAssembler.toResource(submissionEnvelopes);
    }

    @RequestMapping(value = "/projects")
    public PagedResources<Resource<Project>> getUserProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findByUser(getPrincipal(), pageable);
        return projectPagedResourcesAssembler.toResource(projects);
    }

    private String getPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.toString();
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(UserController.class).withRel("user"));
        return resource;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class Summary {

        private Long draftSubmissions = 0L;
        private Long pendingSubmissions = 0L;
        private Long completedSubmissions = 0L;
        private Long projects = 0L;

        public Summary(long pendingSubmissions, long draftSubmissions, long completedSubmissions, long projects) {
            this.pendingSubmissions = pendingSubmissions;
            this.draftSubmissions = draftSubmissions;
            this.completedSubmissions = completedSubmissions;
            this.projects = projects;
        }
    }

}
