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
        assert mockSchemaUris.size() == 108;
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
