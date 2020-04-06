package org.humancellatlas.ingest.security.web;

import org.humancellatlas.ingest.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.humancellatlas.ingest.security.GcpConfig.GCP;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebMvcTest
@SpringJUnitWebConfig({ AuthenticationController.class, SecurityConfig.class })
public class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean(name=GCP)
    private AuthenticationProvider gcp;

    @MockBean(name=ELIXIR)
    private AuthenticationProvider elixir;

    private MockMvc webApp;

    @BeforeEach
    void setUp() {
        webApp = webAppContextSetup(applicationContext)
                .alwaysDo(print())
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        void withValidJwt() throws Exception {
            // expect:
            webApp.perform(post("/auth/registration")
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

    }

}
