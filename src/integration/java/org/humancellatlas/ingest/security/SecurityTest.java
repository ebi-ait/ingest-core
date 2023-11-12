package org.humancellatlas.ingest.security;

import org.hamcrest.CoreMatchers;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.Ignore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class SecurityTest {
    @Autowired
    private MockMvc webApp;

    public static Stream<Arguments> metadataTypes() {
        return Stream.of(
                Arguments.of("files"),
                Arguments.of("biomaterials"),
                Arguments.of("protocols"),
                Arguments.of("processes")
        );
    }

    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

    @Nested
    class Authorised {
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser
        public void apiAccessWithTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrlIsOk("/" + metadataTypePlural + "/");
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser
        public void apiAccessNoTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrlIsOk("/" + metadataTypePlural);
        }

    }

    @Nested
    class Unauthorised {

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        public void apiAccessWithTrailingSlashIsBlocked(String metadataTypePlural) throws Exception {
            checkGetUrlIsUnauthorized("/" + metadataTypePlural + "/");
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        public void apiAccessNoTrailingSlashIsBlocked(String metadataTypePlural) throws Exception {
            checkGetUrlIsUnauthorized("/" + metadataTypePlural);
        }


    }

    @Nested
    class RootResource {
        @Test
        public void checkUnauthenticatedJson_IsAllowed() throws Exception {
            webApp.perform(get("/")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("_links").hasJsonPath());
        }

        @Test
        public void checkUnauthenticatedHtml_IsAllowed() throws Exception {
            webApp.perform(get("/browser/index.html")
                            .accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(content().string(CoreMatchers.containsString("The HAL Browser (for Spring Data REST)")));
        }
    }

    @Nested
    class HealthResource {
//        @ParameterizedTest
        @Ignore
        @ValueSource(strings = {"health","info","prometheus"})
        public void checkUnauthenticatedJson_IsAllowed(String endpoint) throws Exception {
            webApp.perform(get("/"+endpoint))
                    .andExpect(status().isOk());
        }
    }

    private void checkGetUrlIsUnauthorized(String url) throws Exception {
        webApp.perform(
                get(url)
        ).andExpect(status().isUnauthorized());
    }

    private void checkGetUrlIsOk(String url) throws Exception {
        webApp.perform(
                get(url)
        ).andExpect(status().isOk());
    }
}
