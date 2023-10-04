package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo()
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
    public static Stream<Arguments> metadataTypesWithProject() {
        return Stream.concat(
                metadataTypes(),
                Stream.of(Arguments.of("projects" ))
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
        public void autorizedApiAccessWithTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrlIsAuthorized("/" + metadataTypePlural + "/");
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser
        public void unautorizedApiAccessNoTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrlIsAuthorized("/" + metadataTypePlural);
        }

    }
    @Nested
    class Unauthorised {

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        public void unautorizedApiAccessWithTrailingSlashIsBlocked(String metadataTypePlural) throws Exception {
            checkGetUrlIsUnauthorized("/" + metadataTypePlural + "/");
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")

        public void unautorizedApiAccessNoTrailingSlashIsBlocked(String metadataTypePlural) throws Exception {
            checkGetUrlIsUnauthorized("/" + metadataTypePlural);
        }
    }


    private void checkGetUrlIsUnauthorized(String url) throws Exception {
        webApp.perform(
                get(url)
        ).andExpect(status().isUnauthorized());
    }
    private void checkGetUrlIsAuthorized(String url) throws Exception {
        webApp.perform(
                get(url)
        ).andExpect(status().isOk());
    }
}
