package uk.ac.ebi.subs.ingest.exporter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import uk.ac.ebi.subs.ingest.core.web.LinkGenerator;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.messaging.model.ExportEntityMessage;
import uk.ac.ebi.subs.ingest.messaging.model.ManifestMessage;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

public class ExperimentProcess {

  private final int index;
  private final int totalCount;

  private final Process process;

  private final SubmissionEnvelope submissionEnvelope;

  private final Project project;

  public ExperimentProcess(
      int index, int totalCount, Process process, SubmissionEnvelope submission, Project project) {
    this.index = index;
    this.totalCount = totalCount;
    this.process = process;
    this.submissionEnvelope = submission;
    this.project = project;
  }

  public static ExperimentProcess from(Process process, IndexCounter counter) {
    return new ExperimentProcess(
        counter.next(),
        counter.totalCount,
        process,
        process.getSubmissionEnvelope(),
        process.getProject());
  }

  public static class IndexCounter {
    int base;
    int totalCount;

    IndexCounter(int totalCount) {
      this.base = 0;
      this.totalCount = totalCount;
    }

    int next() {
      return base++;
    }
  }

  public Integer getIndex() {
    return index;
  }

  public Integer getTotalCount() {
    return totalCount;
  }

  public Process getProcess() {
    return process;
  }

  public SubmissionEnvelope getSubmissionEnvelope() {
    return submissionEnvelope;
  }

  public ExportEntityMessage toExportEntityMessage(
      LinkGenerator linkGenerator, ExportJob exportJob, Map<String, Object> context) {
    String callbackLink = linkGenerator.createCallback(process.getClass(), process.getId());
    return new ExportEntityMessage(
        exportJob.getId(),
        process.getId(),
        process.getUuid().toString(),
        callbackLink,
        process.getClass().getSimpleName().toLowerCase(),
        submissionEnvelope.getId(),
        submissionEnvelope.getUuid().toString(),
        project.getId(),
        process.getProject().getUuid().toString(),
        index,
        totalCount,
        context);
  }

  public ManifestMessage toManifestMessage(LinkGenerator linkGenerator) {
    String callbackLink = linkGenerator.createCallback(process.getClass(), process.getId());
    return new ManifestMessage(
        UUID.randomUUID(),
        Instant.now().toString(),
        process.getId(),
        process.getUuid().toString(),
        callbackLink,
        process.getClass().getSimpleName(),
        submissionEnvelope.getId(),
        submissionEnvelope.getUuid().toString(),
        index,
        totalCount);
  }
}
