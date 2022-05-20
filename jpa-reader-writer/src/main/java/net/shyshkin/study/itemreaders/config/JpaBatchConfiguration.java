package net.shyshkin.study.itemreaders.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemreaders.model.Product;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JpaBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Autowired
    private EntityManagerFactory factory;

    @Bean
    public Job jpaJob() {
        return jobs.get("jpaJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaStep())
                .build();
    }

    @Bean
    Step jpaStep() {
        return steps.get("readCsv")
                .<Product, Product>chunk(3)
                .reader(reader())
                .writer(items -> items.forEach(item -> log.debug("{}", item)))
                .build();
    }

    @Bean
    JpaCursorItemReader<Product> reader() {
        return new JpaCursorItemReaderBuilder<Product>()
                .name("jpaReader")
                .queryString("select p from Product p")
                .entityManagerFactory(factory)
                .build();
    }

}
