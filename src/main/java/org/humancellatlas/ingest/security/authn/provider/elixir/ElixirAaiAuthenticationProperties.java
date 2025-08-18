package org.humancellatlas.ingest.security.authn.provider.elixir;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@RequiredArgsConstructor
@Component
@ConfigurationProperties(prefix = "elixir")
public class ElixirAaiAuthenticationProperties {
    private String issuerWhitelist = "aai.lifescience-ri"; // changed from "elixir", see https://github.com/ebi-ait/dcp-ingest-central/issues/1069
}
