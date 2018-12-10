package org.humancellatlas.ingest.core.web;

import com.mongodb.DB;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.strategy.impl.ProcessCrudStrategy;
import org.humancellatlas.ingest.core.service.strategy.impl.ProjectCrudStrategy;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringLinkGeneratorTest {

    @Autowired
    private LinkGenerator linkGenerator;

    @Autowired
    private ResourceMappings mappings;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    private BiomaterialRepository biomaterialRepository;

    @MockBean
    ProcessRepository processRepository;

    @MockBean
    HateoasPageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver;

    @MockBean
    ProcessCrudStrategy processCrudStrategy;

    @MockBean
    MongoTemplate mongoTemplate;

    @MockBean
    ProtocolRepository protocolRepository;

    @MockBean
    ProjectCrudStrategy projectCrudStrategy;

    @MockBean
    FileService fileService;

    @MockBean
    MetadataDocumentResourceProcessor metadataDocumentResourceProcessor;

    @MockBean
    RepositoryResourceMappings repositoryResourceMappings;

    @MockBean
    FileRepository fileRepository;

    @MockBean
    BundleManifestRepository bundleManifestRepository;

    @MockBean
    Repositories repositories;

    @MockBean
    RepositoryRestConfiguration repositoryRestConfiguration;

    @MockBean
    ControllerLinkBuilderFactory controllerLinkBuilderFactory;



    @Test
    public void testCreateCallback() {
        //when:
        String processCallback = linkGenerator.createCallback(Process.class, "df00e2");
        String biomaterialCallback = linkGenerator.createCallback(Biomaterial.class, "c80122");
        String fileCallback = linkGenerator.createCallback(File.class, "98dd90");
        String protocolCallback = linkGenerator.createCallback(Protocol.class, "846df1");
        String bmCallback = linkGenerator.createCallback(BundleManifest.class, "332fd9");

        //then:
        assertThat(processCallback).isEqualToIgnoringCase("/processes/df00e2");
        assertThat(biomaterialCallback).isEqualToIgnoringCase("/biomaterials/c80122");
        assertThat(fileCallback).isEqualToIgnoringCase("/files/98dd90");
        assertThat(protocolCallback).isEqualToIgnoringCase("/protocols/846df1");
        assertThat(bmCallback).isEqualToIgnoringCase("/bundlemanifests/332fd9");
    }

    @TestConfiguration
    static class TestApplication {

        @Bean
        MongoDbFactory doNotConnectToLiveDatabase() {
            return new MongoDbFactory() {

                @Override
                public DB getDb() throws DataAccessException {
                    return null;
                }

                @Override
                public DB getDb(String dbName) throws DataAccessException {
                    return null;
                }

                @Override
                public PersistenceExceptionTranslator getExceptionTranslator() {
                    return null;
                }

            };
        }

    }

}
