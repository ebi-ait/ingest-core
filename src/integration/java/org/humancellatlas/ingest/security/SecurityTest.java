package org.humancellatlas.ingest.security;

import org.hamcrest.CoreMatchers;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.Ignore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
    public static Stream<Arguments> metadataTypesWithProjectAndSubmission() {
        return Stream.concat(
                metadataTypes(),
                Stream.of(
                        Arguments.of("projects" ),
                        Arguments.of("submissionEnvelopes" )
                )
        );
    }
    public static Stream<Arguments> metadataTypesWithSubmission() {
        return Stream.concat(
                metadataTypes(),
                Stream.of(
                        Arguments.of("submissionEnvelopes" )
                )
        );
    }

    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

//    @Nested
    @Ignore()
    class Authorised {
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        @WithMockUser
        public void apiAccessWithTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural + "/");
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        @WithMockUser
        public void apiAccessNoTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural);
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void singleMetadataDocumentAccessible(String metadataTypePlural) throws Exception {
            // Getting "not found" means that the request passed the security configuration
            webApp.perform(
                    get("/" + metadataTypePlural + "/"+"abc123")
            ).andExpect(status().isNotFound());
        }
    }

    @Nested
    class Unauthorised {
        private static final String FORWARDED_HOST = "x-forwarded-host";

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void apiAccessWithTrailingSlash_IsAllowed(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural + "/");
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithSubmission")
        public void proxyAccessWithTrailingSlash_IsBlocked(String metadataTypePlural) throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.add(FORWARDED_HOST, "test.com");
            checkGetUrl_IsUnauthorized("/" + metadataTypePlural + "/", headers);
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void search_IsAllowed(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural + "/search");
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithSubmission")
        public void proxySearch_IsBlocked(String metadataTypePlural) throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.add(FORWARDED_HOST, "test.com");
            checkGetUrl_IsUnauthorized("/" + metadataTypePlural + "/search", headers);
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithSubmission")
        public void internalSearchByUuid_IsPermitted(String metadataTypePlural) throws Exception {
            // "not found" means we passed security
            webApp.perform(get("/"+metadataTypePlural+"/search/findByUuidUuid") )
                    .andExpect(status().isNotFound());
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void apiAccessNoTrailingSlash_IsAllowed(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural);
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void proxyApiAccessNoTrailingSlash_IsBlocked(String metadataTypePlural) throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.add(FORWARDED_HOST, "test.com");
            checkGetUrl_IsUnauthorized("/" + metadataTypePlural, headers);
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void internalSingleResource_IsPermitted(String metadataTypePlural) throws Exception {
            // "not found" means we passed security
            webApp.perform(get("/"+metadataTypePlural+"/abc123") )
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithSubmission")
        public void proxySingleResource_IsBlocked(String metadataTypePlural) throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.add(FORWARDED_HOST, "test.com");
            checkGetUrl_IsUnauthorized("/" + metadataTypePlural + "/abc123", headers);
        }
    }

    @Nested
    @WithMockUser(roles = "WRANGLER")
    class WranglerAccess {
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        public void singleMetadataDocument_Accessible(String metadataTypePlural) throws Exception {
            // Getting "not found" means that the request passed the security configuration
            webApp.perform(
                    get("/" + metadataTypePlural + "/"+"abc123")
            ).andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        @WithMockUser
        public void apiAccessWithTrailingSlash_IsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural + "/");
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        @WithMockUser
        public void apiAccessNoTrailingSlash_IsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural);
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        @WithMockUser
        public void apiSearch_IsPermitted(String metadataTypePlural) throws Exception {
            checkGetUrl_IsOk("/" + metadataTypePlural+"/search");
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProjectAndSubmission")
        @WithMockUser
        public void apiSearchByUuid_IsPermitted(String metadataTypePlural) throws Exception {
            // "not found" means we passed security
            webApp.perform(get("/" + metadataTypePlural+"/search/findByUuid") )
                    .andExpect(status().isNotFound());
        }
    }
    @Nested
    class RootResource {
        @Test
        public void unauthenticatedJson_IsAllowed() throws Exception {
            webApp.perform(get("/")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("_links").hasJsonPath());
        }

        @Test
        public void unauthenticatedHtml_IsAllowed() throws Exception {
            webApp.perform(get("/browser/index.html")
                            .accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(content().string(CoreMatchers.containsString("The HAL Browser (for Spring Data REST)")));
        }
    }

    @Nested
    class ManagementResources {
//        @ParameterizedTest
        @Ignore("passes locally, fails in gitlab")
        @ValueSource(strings = {"health","info","prometheus"})
        public void checkUnauthenticatedJson_IsAllowed(String endpoint) throws Exception {
            webApp.perform(get("/"+endpoint))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class SchemaResource {

        @Test

        public void unauthenticate_IsAllowed() throws Exception {
            webApp.perform(get("/schemas"))
                    .andExpect(status().isOk());
        }
        @Test
        public void unauthenticatedSubResource_IsAllowed() throws Exception {
            webApp.perform(get("/schemas/search"))
                    .andExpect(status().isOk());
        }
    }

    private void checkGetUrl_IsUnauthorized(String url) throws Exception {
        webApp.perform(get(url))
                .andExpect(status().isUnauthorized());
    }

    private void checkGetUrl_IsUnauthorized(String url, HttpHeaders headers) throws Exception {
        webApp.perform(get(url).headers(headers))
                .andExpect(status().isUnauthorized());
    }

    private void checkGetUrl_IsOk(String url) throws Exception {
        webApp.perform(get(url) )
                .andExpect(status().isOk());
    }
    private void checkGetUrl_IsOk(String url, HttpHeaders headers) throws Exception {
        webApp.perform(get(url).headers(headers))
                .andExpect(status().isOk());
    }
}
