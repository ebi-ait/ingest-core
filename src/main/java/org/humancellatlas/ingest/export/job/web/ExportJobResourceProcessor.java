package org.humancellatlas.ingest.export.job.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExportJobResourceProcessor implements ResourceProcessor<Resource<ExportJob>> {
    private final @NonNull EntityLinks entityLinks;

    private Link getEntitiesLink(ExportJob exportJob) {
        return entityLinks.linkForSingleResource(exportJob)
            .slash(Links.EXPORT_JOB_ENTITIES_URL)
            .withRel(Links.EXPORT_JOB_ENTITIES_REL);
    }

    private Link getEntitiesStatusLink(ExportJob exportJob) {
        return entityLinks.linkForSingleResource(exportJob)
            .slash(Links.EXPORT_JOB_ENTITIES_URL + "?status={status}")
            .withRel(Links.EXPORT_JOB_ENTITIES_BY_STATUS_REL);
    }

    private Link getSubmissionLink(SubmissionEnvelope submission) {
        return entityLinks.linkForSingleResource(submission).withRel("submission");
    }

    private Link getDataTransferLink(ExportJob exportJob) {
        return entityLinks.linkForSingleResource(exportJob)
            .slash(Links.EXPORT_JOB_DATA_TRANSFER_URL)
            .withRel(Links.EXPORT_JOB_DATA_TRANSFER_REL);
    }

    @Override
    public Resource<ExportJob> process(Resource<ExportJob> resource) {
        ExportJob exportJob = resource.getContent();
        resource.add(getEntitiesLink(exportJob));
        resource.add(getEntitiesStatusLink(exportJob));
        resource.add(getSubmissionLink(exportJob.getSubmission()));
        resource.add(getDataTransferLink(exportJob));
        return resource;
    }
}
