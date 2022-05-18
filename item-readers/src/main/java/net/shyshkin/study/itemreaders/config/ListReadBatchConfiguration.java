package net.shyshkin.study.itemreaders.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemreaders.model.Product;
import net.shyshkin.study.itemreaders.service.ProductClient;
import net.shyshkin.study.itemreaders.writer.ConsoleItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class ListReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;
    private final ProductClient productClient;

    @Bean
    public Job listReadJob() {
        return jobs.get("listReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readListStep())
                .build();
    }

    @Bean
    Step readListStep() {
        return steps.get("readList")
                .<Product, Product>chunk(3)
                .reader(listReader())
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    ItemReader<Product> listReader() {
        return new ListItemReader<Product>(productsWrapped());
    }

    private List<Product> productsWrapped() {
        try {
            return productClient.getAllProducts();
        } catch (IOException | InterruptedException e) {
            log.warn("Error occurred {}", e.getMessage());
        }
        return List.of();
    }

}
