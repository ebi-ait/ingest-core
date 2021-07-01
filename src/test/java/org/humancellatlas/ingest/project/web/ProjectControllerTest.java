package org.humancellatlas.ingest.project.web;

import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectEventHandler;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.humancellatlas.ingest.security.GcpConfig.GCP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectController.class)
@WebAppConfiguration
@MockBeans({
        @MockBean(ProjectService.class),
        @MockBean(ValidationStateChangeService.class),
        @MockBean(ProjectEventHandler.class),
        @MockBean(ProjectRepository.class),
        @MockBean(BiomaterialRepository.class),
        @MockBean(ProcessRepository.class),
        @MockBean(ProtocolRepository.class),
        @MockBean(FileRepository.class),
        @MockBean(MetadataUpdateService.class),
        @MockBean(PagedResourcesAssembler.class),
        @MockBean(ProjectResourceProcessor.class)
})
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ComponentScan(basePackages = "org.humancellatlas.ingest.project")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = GCP)
    private AuthenticationProvider gcp;

    @MockBean(name = ELIXIR)
    private AuthenticationProvider elixir;


    @Autowired
    private WebApplicationContext applicationContext;

    private static final String PATH = "/projects/suggestion";

    @Getter @Setter
    static class Suggestion {
        private String doi;
        private String name;
        private String email;
        private String comment;
    }

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .build();
    }

    @Test
    public void whenSuggestingAProjectWithCorrectPayload_ThenReturnsCorrectResponse() throws Exception {
//        Suggestion suggestion = new Suggestion();
//        suggestion.setDoi("doi/123");
//        suggestion.setName("Test User");
//        suggestion.setEmail("test@example.com");
//        suggestion.setComment("This is a comment");
        String payload =
                "DOI: doi/123 Name: Test User \nEmail: test@example.com \nComments: Test comment";

        mockMvc.perform(post(PATH)
//                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
                .andDo(print());
    }
}

