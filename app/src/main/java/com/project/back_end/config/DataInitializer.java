package com.project.back_end.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.models.Prescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

// Seeds MongoDB with sample prescriptions at startup. MySQL sample data is loaded from data.sql.
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public DataInitializer(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed an empty collection, so restarts don't create duplicates
        if (mongoTemplate.count(new Query(), Prescription.class) > 0) {
            return;
        }

        try (InputStream in = new ClassPathResource("data/prescriptions.json").getInputStream()) {
            List<Prescription> prescriptions = objectMapper.readValue(in, new TypeReference<>() {});
            mongoTemplate.insertAll(prescriptions);
            log.info("Inserted {} sample prescriptions", prescriptions.size());
        }
    }
}
