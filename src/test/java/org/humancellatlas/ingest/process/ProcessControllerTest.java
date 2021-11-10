package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProcessControllerTest {
    @MockBean
    ValidationStateChangeService validationStateChangeService;

    @MockBean
    private MessageRouter messageRouter;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProtocolRepository protocolRepository;


   @Test
   public void testDeleteProtocolTriggersValidationStateToDraft() throws Exception {
       // given
       Protocol protocol = new Protocol("protocol");
       protocolRepository.save(protocol);

       // and
       Process process = new Process("process");
       process.addProtocol(protocol);
       processRepository.save(process);

       // send delete request
       webApp.perform(delete("/processes/{processId}/protocols/{protocolId}", process.getId(), protocol.getId()));
      // verify service being called
       verify(validationStateChangeService, times(1)).changeValidationState(any(),any(), eq(ValidationState.DRAFT));
   }

   @Test
   public void testSaveProtocolTriggersValidationStateToDraft() throws Exception {
       // given
       Protocol protocol = new Protocol("protocol");
       protocolRepository.save(protocol);

       // and
       Process process = new Process("process");
//       process.addProtocol(protocol);
       processRepository.save(process);
       System.out.println(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
       // send post request
       webApp.perform(post("/processes/{processId}/protocols/", process.getId())
               .contentType("text/uri-list")
               .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/protocols/" + protocol.getId()));
      // verify service being called
       verify(validationStateChangeService, times(1)).changeValidationState(any(),any(), eq(ValidationState.DRAFT));
   }


}
