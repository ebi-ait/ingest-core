package uk.ac.ebi.subs.ingest.security;


import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import uk.ac.ebi.subs.ingest.security.authn.provider.globus.GlobusBearerAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class MixedBearerSecurityContextRepository implements SecurityContextRepository {

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder holder) {
        HttpServletRequest request = holder.getRequest();

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            if (token.isEmpty()) {
                log.debug("[MixedBearer] Empty bearer token – leaving context anonymous");
                return context;
            }

            try {
                if (token.contains(".")) {
                    // Looks like a JWT → keep Auth0 behaviour for existing flows
                    log.debug("[MixedBearer] Treating token as JWT");
                    context.setAuthentication(
                            PreAuthenticatedAuthenticationJsonWebToken.usingToken(token)
                    );
                } else {
                    // Opaque token (e.g. Globus) → wrap in our own Authentication
                    log.debug("[MixedBearer] Treating token as opaque (Globus) bearer");
                    context.setAuthentication(new GlobusBearerAuthentication(token));
                }
            } catch (Exception e) {
                log.warn("[MixedBearer] Failed to create Authentication from bearer token", e);
            }
        }

        return context;
    }

    @Override
    public void saveContext(SecurityContext context,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        // stateless API – nothing to persist
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        // We always rebuild context per request from the Authorization header
        return false;
    }
}