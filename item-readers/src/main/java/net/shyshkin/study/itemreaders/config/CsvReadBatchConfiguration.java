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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class CsvReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;

    @Bean
    public Job csvReadJob() {
        return jobs.get("csvReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readCsvStep())
                .build();
    }

    @Bean
    Step readCsvStep() {
        return steps.get("readCsv")
                .<Product, Product>chunk(3)
                .reader(flatFileItemReader(null))
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemReader<Product> flatFileItemReader(@Value("#{jobParameters['inputFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productCsvReader")
                .resource(inputFile)
                .linesToSkip(1)
                .delimited()
                .delimiter("|")
                .names("productID", "productName", "productDesc", "price", "unit")
                .targetType(Product.class)
                .build();
    }

}
