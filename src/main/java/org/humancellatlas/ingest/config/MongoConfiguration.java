package org.humancellatlas.ingest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "userAuditing")
public class MongoConfiguration {}
