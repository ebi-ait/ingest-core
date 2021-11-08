package org.humancellatlas.ingest.process.web;

import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProcessControllerTest {
    @MockBean
    ValidationStateChangeService validationStateChangeService;
    @Autowired
    private MockMvc webApp;

   public void testDeleteTriggersValidationSTateToDraft() throws Exception {
       // send delete request
       String processId = "testProcess";
       String protocolId = "testProtocol";
       MvcResult result = webApp
               .perform(delete("/processes/%s/protocols/%s", processId, protocolId))
               .andReturn();
       // verify service being called
       verify(validationStateChangeService).changeValidationState(any(),any(), ValidationState.DRAFT);
   }
}
