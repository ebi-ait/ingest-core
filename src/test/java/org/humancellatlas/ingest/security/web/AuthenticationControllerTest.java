package org.humancellatlas.ingest.security.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringJUnitWebConfig({AuthenticationController.class})
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc webApp;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @WithMockUser
        void withValidJwt() throws Exception {
            // expect:
            webApp.perform(post("/auth/registration").with(csrf()))
                    .andExpect(status().isOk());
        }

    }

}
