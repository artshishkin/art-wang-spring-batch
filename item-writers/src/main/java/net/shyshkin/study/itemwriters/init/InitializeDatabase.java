package net.shyshkin.study.itemwriters.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("mysql")
public class InitializeDatabase implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("classpath:jdbc/products-schema.sql")
    private Resource productsSchema;

    @Override
    public void run(String... args) throws Exception {
        String schemaSql = FileCopyUtils.copyToString(new InputStreamReader(productsSchema.getInputStream()));
        log.debug("Create SQL: {}", schemaSql);
        jdbcTemplate.execute(schemaSql);
    }
}
