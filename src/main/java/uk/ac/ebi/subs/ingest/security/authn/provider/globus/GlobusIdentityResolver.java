package uk.ac.ebi.subs.ingest.security.authn.provider.globus;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.ingest.security.Account;

@Component
public class GlobusIdentityResolver {

    private static final String GLOBUS_ID_HEADER = "X-Globus-Identity";

    // Spring injects a request-scoped proxy here; safe to use in a singleton @Component
    @Autowired
    private HttpServletRequest request;

    /**
     * Resolve the Globus principal id to use for ACLs.
     *
     * Priority:
     *   1) X-Globus-Identity header  (for legacy / Cognito-based clients)
     *   2) Account.providerReference (for Globus-authenticated users)
     *   3) auth.getName() as a last-resort fallback
     */
    public String getCurrentGlobusPrincipalId() {
        // 1) Header fallback — keeps Marcin unblocked while he still uses Cognito
        String headerId = request.getHeader(GLOBUS_ID_HEADER);
        if (headerId != null && !headerId.isBlank()) {
            return headerId.trim();
        }

        // 2) Normal path: use the authenticated Account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authentication in security context");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Account) {
            String providerRef = ((Account) principal).getProviderReference();
            if (providerRef != null && !providerRef.isBlank()) {
                return providerRef.trim();
            }
        }

        // 3) Last resort – probably indicates a misconfiguration
        String name = auth.getName();
        if (name != null && !name.isBlank()) {
            return name.trim();
        }

        throw new IllegalStateException(
                "Unable to resolve Globus principal id: no X-Globus-Identity header and providerReference is empty"
        );
    }
}
