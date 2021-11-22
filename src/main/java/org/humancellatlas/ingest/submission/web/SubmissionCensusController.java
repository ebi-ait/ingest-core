package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
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
        return new SubmissionCensus(submissionEnvelope);
    }

    @Getter
    public class SubmissionCensus {
        private final UUID uuid;
        private final Dictionary<String, UUID> processes;

        public SubmissionCensus(SubmissionEnvelope submissionEnvelope){
            this.uuid = submissionEnvelope.getUuid().getUuid();
            this.processes = new Hashtable<>();
            Collection<Process> allProcesses = processRepository.findAllBySubmissionEnvelope(submissionEnvelope);
            allProcesses.forEach(process -> this.setDictionary(this.processes, process));
        }

        private void setDictionary(Dictionary<String, UUID> dictionary, AbstractEntity entity){
            dictionary.put(entity.getId(), entity.getUuid().getUuid());
        }
    }
}
