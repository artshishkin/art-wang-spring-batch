package net.shyshkin.study.itemreaders.init;

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
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("mysql")
public class InitializeDatabase implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("classpath:jdbc/products-schema.sql")
    private Resource productsSchema;

    @Value("classpath:jdbc/products-data.sql")
    private Resource productsData;

    @Override
    public void run(String... args) throws Exception {
        String schemaSql = FileCopyUtils.copyToString(new InputStreamReader(productsSchema.getInputStream()));
        log.debug("Create SQL: {}", schemaSql);
        jdbcTemplate.execute(schemaSql);
        Long size = jdbcTemplate.queryForObject("select count(*) from products_jpa", Long.class);
        log.debug("Products table size: {}", size);
        if (size == 0) {
            String dataSql = FileCopyUtils.copyToString(new InputStreamReader(productsData.getInputStream()));
            Stream.of(dataSql.split(";"))
                    .forEach(jdbcTemplate::execute);
        }
    }
}
