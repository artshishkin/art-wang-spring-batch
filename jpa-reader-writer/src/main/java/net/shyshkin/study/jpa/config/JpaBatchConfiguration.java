package net.shyshkin.study.jpa.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.jpa.mapper.ProductMapper;
import net.shyshkin.study.jpa.model.Category;
import net.shyshkin.study.jpa.model.ProductOut;
import net.shyshkin.study.jpa.writer.IterableItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JpaBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final EntityManagerFactory factory;
    private final ProductMapper mapper;

    @Bean
    public Job jpaJob() {
        return jobs.get("jpaJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaStep())
                .build();
    }

    @Bean
    Step jpaStep() {
        return steps.get("readJpa")
                .<Category, List<ProductOut>>chunk(3)
                .reader(reader())
                .processor(productProcessor())
                .writer(productListItemWriter())
                .build();
    }

    @Bean
    ItemProcessor<Category, List<ProductOut>> productProcessor() {
        return category -> category.getProducts()
                .stream()
                .map(product -> mapper.map(product, category))
                .collect(Collectors.toList());
    }

    @Bean
    JpaCursorItemReader<Category> reader() {
        return new JpaCursorItemReaderBuilder<Category>()
                .name("jpaReader")
                .queryString("select c from Category c")
                .entityManagerFactory(factory)
                .build();
    }

    @Bean
    JpaItemWriter<ProductOut> jpaItemWriter() {
        return new JpaItemWriterBuilder<ProductOut>()
                .entityManagerFactory(factory)
                .usePersist(false)
                .build();
    }

    @Bean
    IterableItemWriter<ProductOut> productListItemWriter() {
        return new IterableItemWriter<>(jpaItemWriter());
    }

}
