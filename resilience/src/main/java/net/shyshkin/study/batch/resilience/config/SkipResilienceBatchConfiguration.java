package net.shyshkin.study.batch.resilience.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.listener.ProductSkipListener;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.processor.ProductProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class SkipResilienceBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final FlatFileItemReader<Product> itemReader;
    private final ProductSkipListener productSkipListener;
    private final ProductProcessor productProcessor;

    @Bean
    public Job csvWriteJob() {
        return jobs.get("skipJob")
                .incrementer(new RunIdIncrementer())
                .start(writeCsvStep())
                .build();
    }

    @Bean
    Step writeCsvStep() {
        return steps.get("skipStep")
                .<Product, Product>chunk(3)
                .reader(itemReader)
                .processor(productProcessor)
                .writer(flatFileItemWriter(null))
                .faultTolerant()
//                .skip(FlatFileParseException.class)
//                .skipLimit(10)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(productSkipListener)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemWriter<Product> flatFileItemWriter(@Value("#{jobParameters['csvOutputFile']}") FileSystemResource outputFile) {
        return new FlatFileItemWriterBuilder<Product>()
                .name("csvProductWriter")
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
