package uk.ac.ebi.subs.ingest.exporter;

import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

public interface Exporter {

  void exportManifests(SubmissionEnvelope submissionEnvelope);

  void exportMetadata(ExportJob exportJob);

  void generateSpreadsheet(SubmissionEnvelope envelope);

  void exportData(SubmissionEnvelope submissionEnvelope);

  void generateSpreadsheet(ExportJob exportJob);
}
