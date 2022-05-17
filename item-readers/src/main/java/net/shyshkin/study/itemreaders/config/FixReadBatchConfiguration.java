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
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class FixReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;

    @Bean
    public Job fixReadJob() {
        return jobs.get("fixReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readFixStep())
                .build();
    }

    @Bean
    Step readFixStep() {
        return steps.get("readFix")
                .<Product, Product>chunk(3)
                .reader(fixedItemReader(null))
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemReader<Product> fixedItemReader(@Value("#{jobParameters['fixInputFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productFixReader")
                .resource(inputFile)
                .linesToSkip(1)
                .fixedLength()
                .columns(new Range(1, 16), new Range(17, 40), new Range(41, 64), new Range(65, 72), new Range(73, 81))
                .names("productID", "productName", "productDesc", "price", "unit")
                .targetType(Product.class)
                .build();
    }

}
