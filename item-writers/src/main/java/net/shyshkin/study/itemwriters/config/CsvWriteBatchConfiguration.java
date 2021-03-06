package net.shyshkin.study.itemwriters.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemwriters.model.Product;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
public class CsvWriteBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final FlatFileItemReader<Product> itemReader;

    @Bean
    public Job csvWriteJob() {
        return jobs.get("csvWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(writeCsvStep())
                .build();
    }

    @Bean
    Step writeCsvStep() {
        return steps.get("writeCsv")
                .<Product, Product>chunk(3)
                .reader(itemReader)
                .writer(flatFileItemWriter(null))
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
