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
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JsonReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;

    @Bean
    public Job jsonReadJob() {
        return jobs.get("jsonReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readJsonStep())
                .build();
    }

    @Bean
    Step readJsonStep() {
        return steps.get("readJson")
                .<Product, Product>chunk(3)
                .reader(jsonReader(null))
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    JsonItemReader<Product> jsonReader(@Value("#{jobParameters['jsonInputFile']}") FileSystemResource inputFile) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Product.class);
        return new JsonItemReaderBuilder<Product>()
                .name("productJsonReader")
                .resource(inputFile)
                .jsonObjectReader(new JacksonJsonObjectReader<Product>(Product.class))
                .build();
    }

}
