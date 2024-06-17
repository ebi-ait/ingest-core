package org.humancellatlas.ingest.user;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.Optional;

import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountRepository;
import org.humancellatlas.ingest.security.Role;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.web.SubmissionEnvelopeResourceProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController implements ResourceProcessor<RepositoryLinksResource> {

  @Autowired SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @Autowired ProjectRepository projectRepository;

  @Autowired AccountRepository accountRepository;

  @Autowired
  private PagedResourcesAssembler<SubmissionEnvelope> submissionEnvelopePagedResourcesAssembler;

  @Autowired private PagedResourcesAssembler<Project> projectPagedResourcesAssembler;

  @Autowired private SubmissionEnvelopeResourceProcessor submissionEnvelopeResourceProcessor;

  @Autowired private SelfLinkProvider linkProvider;

  @Autowired private EntityLinks entityLinks;

  @RequestMapping(value = "/summary")
  @ResponseBody
  public Summary summary() {
    String user = getCurrentAccount().getId();
    long pendingSubmissions =
        submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.PENDING, user);
    long draftSubmissions =
        submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.DRAFT, user);
    long completedSubmissions =
        submissionEnvelopeRepository.countBySubmissionStateAndUser(SubmissionState.COMPLETE, user);
    long projects = projectRepository.countByUser(user);
    return new Summary(pendingSubmissions, draftSubmissions, completedSubmissions, projects);
  }

  @RequestMapping(value = "/submissionEnvelopes")
  public PagedResources<Resource<SubmissionEnvelope>> getUserSubmissionEnvelopes(
      Pageable pageable) {
    Page<SubmissionEnvelope> submissionEnvelopes =
        submissionEnvelopeRepository.findByUser(getCurrentAccount().getId(), pageable);
    PagedResources<Resource<SubmissionEnvelope>> pagedResources =
        submissionEnvelopePagedResourcesAssembler.toResource(submissionEnvelopes);
    for (Resource<SubmissionEnvelope> resource : pagedResources) {
      resource.add(entityLinks.linkForSingleResource(resource.getContent()).withRel(Link.REL_SELF));
      submissionEnvelopeResourceProcessor.process(resource);
    }
    return pagedResources;
  }

  @RequestMapping(value = "/projects")
  public PagedResources<Resource<Project>> getUserProjects(Pageable pageable) {
    Page<Project> projects = Page.empty();

    if (getCurrentAccount().getRoles().contains(Role.WRANGLER)) {
      projects =
          projectRepository.findByUserOrPrimaryWrangler(
              getCurrentAccount().getId(), getCurrentAccount().getId(), pageable);
    } else {
      projects = projectRepository.findByUser(getCurrentAccount().getId(), pageable);
    }

    PagedResources<Resource<Project>> pagedResources =
        projectPagedResourcesAssembler.toResource(projects);
    for (Resource<Project> resource : pagedResources) {
      resource.add(entityLinks.linkForSingleResource(resource.getContent()).withRel(Link.REL_SELF));
    }
    return pagedResources;
  }

  private Account getCurrentAccount() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Account account = (Account) authentication.getPrincipal();
    return account;
  }

  @GetMapping(path = "/list", produces = APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<?> listUsers(
      Authentication authentication, @RequestParam("role") Optional<Role> role) {
    if (!authentication.getAuthorities().contains(Role.WRANGLER))
      return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

    return role.map(value -> ResponseEntity.ok(accountRepository.findAccountByRoles(value)))
        .orElseGet(() -> ResponseEntity.ok(accountRepository.findAll()));
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

    public Summary(
        long pendingSubmissions, long draftSubmissions, long completedSubmissions, long projects) {
      this.pendingSubmissions = pendingSubmissions;
      this.draftSubmissions = draftSubmissions;
      this.completedSubmissions = completedSubmissions;
      this.projects = projects;
    }
  }
}
