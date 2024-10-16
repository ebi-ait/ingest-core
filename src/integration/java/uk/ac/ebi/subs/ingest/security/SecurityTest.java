package uk.ac.ebi.subs.ingest.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class SecurityTest {
  @Autowired private MockMvc webApp;

  public static Stream<Arguments> metadataTypes() {
    return Stream.of(
        Arguments.of("files"),
        Arguments.of("biomaterials"),
        Arguments.of("protocols"),
        Arguments.of("processes"));
  }

  public static Stream<Arguments> metadataTypesWithProject() {
    return Stream.concat(metadataTypes(), Stream.of(Arguments.of("projects")));
  }

  @MockBean
  // NOTE: Adding MigrationConfiguration as a MockBean is needed
  // as otherwise MigrationConfiguration won't be initialised.
  private MigrationConfiguration migrationConfiguration;

  @Nested
  class Authorised {
    @ParameterizedTest
    @MethodSource("uk.ac.ebi.subs.ingest.security.SecurityTest#metadataTypes")
    @WithMockUser
    public void apiAccessWithTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
      checkGetUrlIsOk("/" + metadataTypePlural + "/");
    }

    @ParameterizedTest
    @MethodSource("uk.ac.ebi.subs.ingest.security.SecurityTest#metadataTypes")
    @WithMockUser
    public void apiAccessNoTrailingSlashIsPermitted(String metadataTypePlural) throws Exception {
      checkGetUrlIsOk("/" + metadataTypePlural);
    }
  }

  @Nested
  class Unauthorised {

    @ParameterizedTest
    @MethodSource("uk.ac.ebi.subs.ingest.security.SecurityTest#metadataTypes")
    public void apiAccessWithTrailingSlashIsBlocked(String metadataTypePlural) throws Exception {
      checkGetUrlIsUnauthorized("/" + metadataTypePlural + "/");
    }

    @ParameterizedTest
    @MethodSource("uk.ac.ebi.subs.ingest.security.SecurityTest#metadataTypes")
    public void apiAccessNoTrailingSlashIsBlocked(String metadataTypePlural) throws Exception {
      checkGetUrlIsUnauthorized("/" + metadataTypePlural);
    }
  }

  @Nested
  class RootResource {
    @Test
    public void checkUnauthenticatedJson_IsAllowed() throws Exception {
      webApp
          .perform(get("/").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("_links").hasJsonPath());
    }

    @Test
    public void checkUnauthenticatedHtml_IsAllowed() throws Exception {
      webApp
          .perform(get("/browser/index.html").accept(MediaType.TEXT_HTML))
          .andExpect(status().isOk())
          .andExpect(
              content()
                  .string(CoreMatchers.containsString("The HAL Browser (for Spring Data REST)")));
    }
  }

  @Nested
  class ManagementResources {
    //        @ParameterizedTest
    @Ignore
    @ValueSource(strings = {"health", "info", "prometheus"})
    public void checkUnauthenticatedJson_IsAllowed(String endpoint) throws Exception {
      webApp.perform(get("/" + endpoint)).andExpect(status().isOk());
    }
  }

  @Nested
  class SchemaResource {

    @Test
    public void checkUnauthenticate_IsAllowed() throws Exception {
      webApp.perform(get("/schemas")).andExpect(status().isOk());
    }

    @Test
    public void checkUnauthenticatedSubResource_IsAllowed() throws Exception {
      webApp.perform(get("/schemas/search")).andExpect(status().isOk());
    }
  }

  private void checkGetUrlIsUnauthorized(String url) throws Exception {
    webApp.perform(get(url)).andExpect(status().isUnauthorized());
  }

  private void checkGetUrlIsOk(String url) throws Exception {
    webApp.perform(get(url)).andExpect(status().isOk());
  }
}
