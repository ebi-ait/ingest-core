package org.humancellatlas.ingest.schemas;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.humancellatlas.ingest.schemas.schemascraper.impl.SchemaScraperImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


/**
 * Created by rolando on 19/04/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SchemaTest {
    @Autowired SchemaService schemaService;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8088);

    @Test
    public void testSchemaScrape() throws Exception {
        // given
        // an s3 bucket files listing as XML
        SchemaScraper schemaScraper = new SchemaScraperImpl();

        // when
        stubFor(
                get(urlEqualTo("/"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody(new String(Files.readAllBytes(Paths.get(new File(".").getAbsolutePath() + "/src/test/java/org/humancellatlas/ingest/schemas/testfiles/TestBucketListing.xml"))))));

        Collection<URI> mockSchemaUris = schemaScraper.getAllSchemaURIs(URI.create("http://localhost:8088"));

        // we know there are 108 schemas in the test file
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
        SchemaScraper schemaScraper = new SchemaScraperImpl();

        stubFor(
                get(urlEqualTo("/"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody(new String(Files.readAllBytes(Paths.get(new File(".").getAbsolutePath() + "/src/test/java/org/humancellatlas/ingest/schemas/testfiles/TestBucketListing.xml"))))));
        // given
        Collection<URI> mockSchemaUris = schemaScraper.getAllSchemaURIs(URI.create("http://localhost:8088"));

        try {
            // when
            schemaService.schemaDescriptionFromSchemaUris(mockSchemaUris);
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }
}
