package net.shyshkin.study.batch.resilience.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.tasklet.ConsoleTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("restart-batch")
public class RestartBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Bean
    public Job restartJob() {
        return jobs.get("restartJob")
                .start(step0())
                .next(step1())
                .build();
    }

    @Bean
    Step step0() {
        return steps.get("step0")
                .tasklet(new ConsoleTasklet())
                .build();
    }

    @Bean
    Step step1() {
        return steps.get("step1")
                .<Product, Product>chunk(3)
                .reader(restartProductReader(null))
                .writer(restartItemWriter(null))
                .processor(processor())
                .build();
    }

    @Bean
    ItemProcessor<Product, Product> processor() {
        return product -> {
            product.setProductDesc(product.getProductDesc().toUpperCase());
            return product;
        };
    }

    @Bean
    @StepScope
    FlatFileItemReader<Product> restartProductReader(@Value("#{jobParameters['restartCsvInputFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productCsvReader")
                .resource(inputFile)
                .linesToSkip(1)
                .delimited()
                .names("productID", "productName", "productDesc", "price", "unit")
                .targetType(Product.class)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemWriter<Product> restartItemWriter(@Value("#{jobParameters['restartCsvOutputFile']}") FileSystemResource outputFile) {
        return new FlatFileItemWriterBuilder<Product>()
                .name("restartCsvWriter")
                .resource(outputFile)
                .delimited()
                .delimiter("|")
                .names("productID", "productName", "price", "unit", "productDesc")
                .append(true)
                .headerCallback(writer -> writer.write("productID|productName|price|unit|productDesc"))
                .build();
    }

}
