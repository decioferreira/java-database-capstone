package com.project.back_end.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.models.Prescription;
import com.project.back_end.repo.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.List;

// Seeds sample data at startup: MySQL from data.sql, MongoDB from data/prescriptions.json.
// Each store is only seeded when empty, so restarts never create duplicates
// (data.sql has plain INSERTs without ids, so running it twice would copy every row).
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;
    private final DoctorRepository doctorRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public DataInitializer(DataSource dataSource,
                           DoctorRepository doctorRepository,
                           MongoTemplate mongoTemplate,
                           ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.doctorRepository = doctorRepository;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        seedMySql();
        seedMongo();
    }

    private void seedMySql() {
        if (doctorRepository.count() > 0) {
            return;
        }
        new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(dataSource);
        log.info("Inserted MySQL sample data from data.sql");
    }

    private void seedMongo() throws Exception {
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
