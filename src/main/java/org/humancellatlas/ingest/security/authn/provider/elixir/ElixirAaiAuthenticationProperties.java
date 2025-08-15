package org.humancellatlas.ingest.security.authn.provider.elixir;


import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "elixir")
@Component
@Getter
public class ElixirAaiAuthenticationProperties {
    private String issuerWhitelist = "aai.lifescience-ri"; // changed from elixir, see https://github.com/ebi-ait/dcp-ingest-central/issues/1069
}
