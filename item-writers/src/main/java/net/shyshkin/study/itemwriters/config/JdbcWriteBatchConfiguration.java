package net.shyshkin.study.itemwriters.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemwriters.model.Product;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JdbcWriteBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final FlatFileItemReader<Product> itemReader;
    private final DataSource dataSource;

    @Bean
    public Job jdbcWriteJob() {
        return jobs.get("jdbcWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(writeJdbcStep())
                .build();
    }

    @Bean
    Step writeJdbcStep() {
        return steps.get("writeJdbc")
                .<Product, Product>chunk(3)
                .reader(itemReader)
                .writer(jdbcItemWriter())
                .build();
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<Product> jdbcItemWriter() {

        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into products (prod_id, prod_name, prod_desc, price, unit) VALUES (:productID,:productName,:productDesc,:price,:unit)")
                .build();
    }

}
