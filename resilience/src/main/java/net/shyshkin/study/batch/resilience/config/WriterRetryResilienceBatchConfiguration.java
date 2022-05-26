package net.shyshkin.study.batch.resilience.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.exception.RetryableWriterException;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.model.ProductCut;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("retry-in-writer")
public class WriterRetryResilienceBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Bean
    public Job writerRetryJob() {
        return jobs.get("writerRetryJob")
                .incrementer(new RunIdIncrementer())
                .start(writerRetryStep())
                .build();
    }

    @Bean
    Step writerRetryStep() {
        return steps.get("writerRetryStep")
                .<ProductCut, Product>chunk(3)
                .reader(productCutReader(null))
                .writer(writerRetryConsoleItemWriter())
                .processor(processor())
                .faultTolerant()
                .retryLimit(5)
                .retry(RetryableWriterException.class)
                .build();
    }

    @Bean
    ItemProcessor<ProductCut, Product> processor() {
        return new ItemProcessor<ProductCut, Product>() {
            @Override
            public Product process(ProductCut cut) throws Exception {
                log.debug("Fetching product from external service... with id: {}", cut.getProductID());
                Product combined = Product.builder()
                        .productID(cut.getProductID())
                        .productName(cut.getProductName())
                        .productDesc("Some fake description " + cut.getProductName())
                        .unit(cut.getProductID() * 10)
                        .price(new BigDecimal(cut.getProductID() * 111))
                        .build();
                log.debug("Product combined: {}", combined);
                return combined;
            }
        };
    }

    @Bean
    @StepScope
    FlatFileItemReader<ProductCut> productCutReader(@Value("#{jobParameters['writerRetryCsvInputFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<ProductCut>()
                .name("writerRetryCsvReader")
                .resource(inputFile)
                .linesToSkip(1)
                .delimited()
                .names("productID", "productName")
                .targetType(ProductCut.class)
                .build();
    }

    @Bean
    ItemWriter<Product> writerRetryConsoleItemWriter() {
        return new ItemWriter<Product>() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public void write(List<? extends Product> products) throws Exception {
                log.debug("Item Writer output starts:");
                for (Product product : products) {
                    if (product.getProductID() == 2L && counter.incrementAndGet() % 3 != 0) {
                        throw new RetryableWriterException("Exception in Writer Occurred");
                    }
                    System.out.println(product);
                }
            }
        };
    }

}
