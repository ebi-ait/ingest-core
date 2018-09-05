package org.humancellatlas.ingest.config;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by rolando on 05/09/2018.
 */
@Component
public class ConfigurationService implements InitializingBean {
    @Value("${STATE_TRACKER_SCHEME:PLS_CONFIGURE_STATE_TRACKER_SCHEME_ENV_VAR}")
    private String stateTrackerSchemeString;
    @Value("${STATE_TRACKER_HOST:PLS_CONFIGURE_STATE_TRACKER_HOST_ENV_VAR}")
    private String stateTrackerHostString;
    @Value("${STATE_TRACKER_PORT:PLS_CONFIGURE_STATE_TRACKER_PORT_ENV_VAR}")
    private String stateTrackerPortString;
    @Value("${STATE_TRACKER_DOCUMENT_STATES_PATH:PLS_CONFIGURE_STATE_TRACKER_DOCUMENT_STATES_PATH_ENV_VAR}")
    private String documentStatesPathString;

    @Getter
    private String stateTrackerScheme;
    @Getter
    private String stateTrackerHost;
    @Getter
    private int stateTrackerPort;
    @Getter
    private String documentStatesPath;

    private void init(){
        this.stateTrackerScheme = this.stateTrackerSchemeString;
        this.stateTrackerHost = this.stateTrackerHostString;
        this.stateTrackerPort = Integer.parseInt(this.stateTrackerPortString);
        this.documentStatesPath = this.documentStatesPathString;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
