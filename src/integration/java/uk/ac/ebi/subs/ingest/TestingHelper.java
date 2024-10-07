package uk.ac.ebi.subs.ingest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;

public class TestingHelper {
  /**
   * WebMvc resets the security context when it finishes, so the user declared in WithMockUser is
   * deleted. see this StackOVerflow post:
   * https://stackoverflow.com/questions/51622300/mockmvc-seems-to-be-clear-securitycontext-after-performing-request-java-lang-il
   */
  public static void resetTestingSecurityContext() {
    SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
  }
}
