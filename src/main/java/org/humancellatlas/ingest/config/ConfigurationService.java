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
    @Value("${STATE_TRACKER_SCHEME:http}")
    private String stateTrackerSchemeString;
    @Value("${STATE_TRACKER_HOST:localhost}")
    private String stateTrackerHostString;
    @Value("${STATE_TRACKER_PORT:8999}")
    private String stateTrackerPortString;
    @Value("${STATE_TRACKER_DOCUMENT_STATES_PATH:machine-reports}")
    private String documentStatesPathString;
    @Value("${STATE_TRACKER_DOCUMENT_STATES_UPDATE_PATH:state-updates/metadata-documents}")
    private String documentStatesUpdatePathString;
    @Value("${STATE_TRACKER_DOCUMENT_PARAM:metadataDocumentId}")
    private String documentIdParamNameString;

    @Getter
    private String stateTrackerScheme;
    @Getter
    private String stateTrackerHost;
    @Getter
    private int stateTrackerPort;
    @Getter
    private String documentStatesPath;
    @Getter
    private String documentStatesUpdatePath;
    @Getter
    private String documentIdParamName;

    private void init(){
        this.stateTrackerScheme = this.stateTrackerSchemeString;
        this.stateTrackerHost = this.stateTrackerHostString;
        this.stateTrackerPort = Integer.parseInt(this.stateTrackerPortString);
        this.documentStatesPath = this.documentStatesPathString;
        this.documentStatesUpdatePath = this.documentStatesUpdatePathString;
        this.documentIdParamName = this.documentIdParamNameString;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
