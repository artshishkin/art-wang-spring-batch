package net.shyshkin.study.itemreaders.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemreaders.model.Product;
import net.shyshkin.study.itemreaders.writer.ConsoleItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JdbcReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;
    private final DataSource dataSource;

    @Bean
    public Job jdbcReadJob() {
        return jobs.get("jdbcReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readJdbcStep())
                .build();
    }

    @Bean
    Step readJdbcStep() {
        return steps.get("readJdbc")
                .<Product, Product>chunk(3)
                .reader(databaseItemReader())
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    JdbcCursorItemReader<Product> databaseItemReader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("productJdbcReader")
                .dataSource(dataSource)
//                .sql("select * from products")
                .sql("select prod_id as productID, prod_name as product_name, prod_desc as product_desc, price, unit from products")
                .beanRowMapper(Product.class)
                .build();
    }

}
