package net.shyshkin.study.batch.resilience.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.exception.ProductServiceException;
import net.shyshkin.study.batch.resilience.exception.Service500Exception;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.service.ProductService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@EnableRetry
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class RetryResilienceBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ProductService productService;

    @Bean
    public Job retryJob() {
        return jobs.get("retryJob")
                .incrementer(new RunIdIncrementer())
                .start(retryStep())
                .build();
    }

    @Bean
    Step retryStep() {
        return steps.get("retryStep")
                .<Product, Product>chunk(3)
                .reader(serviceItemReader())
                .writer(retryCsvItemWriter(null))
                .faultTolerant()
//                .retryLimit(5)
//                .retry(Service500Exception.class)
//                .retry(ProductServiceException.class)
                .build();
    }

    @Bean
    ItemReader<? extends Product> serviceItemReader() {
        return new ItemReader<Product>() {
            @Override
            @Retryable(include = {Service500Exception.class, ProductServiceException.class}, maxAttempts = 5)
            public Product read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                return productService.getProduct();
            }
        };
    }

    @Bean
    @StepScope
    FlatFileItemWriter<Product> retryCsvItemWriter(@Value("#{jobParameters['retryCsvOutputFile']}") FileSystemResource outputFile) {
        return new FlatFileItemWriterBuilder<Product>()
                .name("retryCsvProductWriter")
                .resource(outputFile)
                .delimited()
                .delimiter("|")
                .names("productID", "productName", "price", "unit", "productDesc")
                .append(false)
                .headerCallback(writer -> writer.write("productID|productName|price|unit|productDesc"))
                .footerCallback(writer -> writer.write(
                                String.format("The file was created %s\n",
                                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        )
                )
                .build();
    }

}
