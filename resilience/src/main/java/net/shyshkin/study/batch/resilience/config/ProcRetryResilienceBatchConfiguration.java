package net.shyshkin.study.batch.resilience.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.exception.Service500Exception;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.model.ProductCut;
import net.shyshkin.study.batch.resilience.service.ProductClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class ProcRetryResilienceBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ProductClient productClient;

    @Bean
    public Job procRetryJob() {
        return jobs.get("procRetryJob")
                .incrementer(new RunIdIncrementer())
                .start(procRetryStep())
                .build();
    }

    @Bean
    Step procRetryStep() {
        return steps.get("procRetryStep")
                .<ProductCut, Product>chunk(3)
                .reader(procProductCutReader(null))
                .writer(procRetryCsvItemWriter(null))
                .processor(processor())
                .faultTolerant()
                .retryLimit(5)
                .retry(Service500Exception.class)
                .retry(ConnectException.class)
                .build();
    }

    @Bean
    ItemProcessor<ProductCut, Product> processor() {
        return new ItemProcessor<ProductCut, Product>() {
            @Override
            public Product process(ProductCut cut) throws Exception {
                log.debug("Fetching product from external service... with id: {}", cut.getProductID());
                Product fetched = productClient.getProduct(cut.getProductID());
                Product combined = Product.builder()
                        .productID(cut.getProductID())
                        .productName(cut.getProductName())
                        .productDesc(fetched.getProductDesc())
                        .unit(fetched.getUnit())
                        .price(fetched.getPrice())
                        .build();
                log.debug("Product combined: {}", combined);
                return combined;
            }
        };
    }

    @Bean
    @StepScope
    FlatFileItemReader<ProductCut> procProductCutReader(@Value("#{jobParameters['procRetryCsvInputFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<ProductCut>()
                .name("procRetryCsvReader")
                .resource(inputFile)
                .linesToSkip(1)
                .delimited()
                .names("productID", "productName")
                .targetType(ProductCut.class)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemWriter<Product> procRetryCsvItemWriter(@Value("#{jobParameters['procRetryCsvOutputFile']}") FileSystemResource outputFile) {
        return new FlatFileItemWriterBuilder<Product>()
                .name("procRetryCsvProductWriter")
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
