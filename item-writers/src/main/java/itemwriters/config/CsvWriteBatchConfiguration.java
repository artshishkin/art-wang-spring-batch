package itemwriters.config;

import itemwriters.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .start(readCsvStep())
                .build();
    }

    @Bean
    Step readCsvStep() {
        return steps.get("readCsv")
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
                .build();
    }

}
