package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class SubmissionCensusController {

    @Autowired
    BiomaterialRepository biomaterialRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    ProcessRepository processRepository;
    @Autowired
    ProtocolRepository protocolRepository;


    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/census", method = RequestMethod.GET)
    @ResponseBody
    public SubmissionCensus submissionCensus(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {
        SubmissionCensus submissionCensus = new SubmissionCensus();
        submissionCensus.setUuid(submissionEnvelope.getUuid().getUuid());

        return submissionCensus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SubmissionCensus {
        private UUID uuid;

    }
    
}
