package org.humancellatlas.ingest.schemas;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.humancellatlas.ingest.schemas.schemascraper.impl.S3BucketSchemaScraper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.doReturn;


/**
 * Created by rolando on 19/04/2018.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SchemaScraperTest {
    @Autowired SchemaService schemaService;

    @MockBean SchemaRepository schemaRepository;

    WireMockServer wireMockServer;

    @BeforeEach
    public void setupWireMockServer() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterEach
    public void teardownWireMockServer() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }


    String mockSchemaUri = "http://localhost:8089";

    @Test
    public void testSchemaScrape() throws Exception {
        // given
        // an s3 bucket files listing as XML
        SchemaScraper schemaScraper = new S3BucketSchemaScraper();

        // when
        wireMockServer.stubFor(
                get(urlEqualTo("/"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody(new String(Files.readAllBytes(Paths.get(new File(".").getAbsolutePath() + "/src/test/resources/testfiles/TestBucketListing.xml"))))));

        Collection<URI> mockSchemaUris = schemaScraper.getAllSchemaURIs(URI.create(mockSchemaUri));

        // we know there are 107 schemas in the test file
        assert mockSchemaUris.size() == 107;
    }

    @Test
    public void testSchemaParse_BundleUris() throws Exception {
        try {
            // when
            schemaService.schemaDescriptionFromSchemaUris(Arrays.asList(URI.create("bundle/1.2.3/biomaterial"),
                                                                        URI.create("bundle/2.3.4/links"),
                                                                        URI.create("bundle/1.0/protocols")));
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }

    @Test
    public void testSchemaParse_ModuleUris() throws Exception {
        try {
            // when
            schemaService.schemaDescriptionFromSchemaUris(Arrays.asList(URI.create("module/biomaterial/5.1.0/growth_condition"),
                                                                        URI.create("module/ontology/5.0.0/biological_macromolecule_ontology"),
                                                                        URI.create("module/process/5.1.0/purchased_reagents")));
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }

    @Test
    public void testSchemaParse_TypeUris() throws Exception {
        try {
            // when
            schemaService.schemaDescriptionFromSchemaUris(Arrays.asList(URI.create("type/biomaterial/5.0.1/cell_line"),
                                                                        URI.create("type/biomaterial/5.1.0/organoid"),
                                                                        URI.create("type/file/5.0.0/sequence_file")));
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }

    @Test
    public void testSchemaParse_SubdomainTypeUris() throws Exception {
        try {
            // when
            schemaService.schemaDescriptionFromSchemaUris(Arrays.asList(URI.create("type/process/biomaterial_collection/5.1.0/collection_process"),
                                                                        URI.create("type/process/sequencing/5.0.0/sequencing_process"),
                                                                        URI.create("type/process/sequencing/5.1.0/sequencing_process")));
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }

    @Test
    public void testSchemaParse() throws Exception {
        // pre-given
        SchemaScraper schemaScraper = new S3BucketSchemaScraper();

        wireMockServer.stubFor(get(urlEqualTo("/"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody(new String(Files.readAllBytes(Paths.get(new File(".").getAbsolutePath() + "/src/test/resources/testfiles/TestBucketListing.xml"))))));
        // given
        Collection<URI> mockSchemaUris = schemaScraper.getAllSchemaURIs(URI.create(mockSchemaUri));

        try {
            // when
            schemaService.schemaDescriptionFromSchemaUris(mockSchemaUris);
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }

    @Test
    public void testGetLatestSchemas() throws Exception {
        Schema mockSchemaA = new Schema("mockHighLevel-A", "2.0","mockDomain-A","mockSubdomain-A","mockConcrete-A", "mock.io/mock-schema-a");
        Schema mockSchemaB = new Schema("mockHighLevel-B", "1.9","mockDomain-B","mockSubdomain-B","mockConcrete-B", "mock.io/mock-schema-a");
        Schema mockSchemaOldA = new Schema("mockHighLevel-A", "1.9","mockDomain-A","mockSubdomain-A","mockConcrete-A", "mock.io/mock-schema-duplicate-a");

        doReturn(Arrays.asList(mockSchemaA, mockSchemaB, mockSchemaOldA))
                .when(schemaRepository).findAll();

        Collection<Schema> latestSchemas = schemaService.getLatestSchemas();
        assert latestSchemas.size() == 2;
        latestSchemas.forEach(schema -> {
            if(schema.getSchemaUri().equals("mock.io/mock-schema-duplicate-a")){
                assert false;
            }
        });
        assert true;
    }

    @Test
    public void testFilterLatestSchemas() throws Exception {
        Schema mockSchemaA = new Schema("mockHighLevel-A", "2.0","mockDomain-A","mockSubdomain-A","mockConcrete-A", "mock.io/mock-schema-a");
        Schema mockSchemaB = new Schema("mockHighLevel-B", "1.9","mockDomain-B","mockSubdomain-B","mockConcrete-B", "mock.io/mock-schema-a");
        Schema mockSchemaOldA = new Schema("mockHighLevel-A", "1.9","mockDomain-A","mockSubdomain-A","mockConcrete-A", "mock.io/mock-schema-duplicate-a");

        doReturn(Arrays.asList(mockSchemaA, mockSchemaB, mockSchemaOldA))
                .when(schemaRepository).findAll();

        Collection<Schema> latestSchemas = schemaService.filterLatestSchemas("mockHighLevel-B");
        assert latestSchemas.size() == 1;
        latestSchemas.forEach(schema -> {
            assert schema.getHighLevelEntity().equals("mockHighLevel-B");
        });
    }

    @Configuration
    class MockConfiguration {
        @Autowired SchemaScraper schemaScraper;
        @Autowired MockEnvironment mockEnvironment;

        @Bean
        SchemaService schemaService() {
            return new SchemaService(schemaRepository, schemaScraper, mockEnvironment);
        }

        @Bean
        SchemaScraper schemaScraper() {
            return new S3BucketSchemaScraper();
        }

    }

}
