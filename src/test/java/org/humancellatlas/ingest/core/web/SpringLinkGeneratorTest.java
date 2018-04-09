package org.humancellatlas.ingest.core.web;

import com.mongodb.DB;
import org.humancellatlas.ingest.process.Process;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringLinkGeneratorTest {

    @Autowired
    private LinkGenerator linkGenerator;

    @Autowired
    private ResourceMappings mappings;

    @Test
    public void testCreateCallback() {
        //when:
        String callbackLink = linkGenerator.createCallback(Process.class, "df00e2");

        //then:
        assertThat(callbackLink).isNotNull();
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
