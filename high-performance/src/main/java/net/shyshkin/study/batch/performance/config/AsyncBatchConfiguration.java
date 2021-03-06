package net.shyshkin.study.batch.performance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.model.Product;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Future;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("async")
public class AsyncBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final FlatFileItemReader<Product> itemReader;

    @Value("${app.processor.pause}")
    private Long fakeProcessorPause;

    @Bean
    public Job asyncJob() {
        return jobs.get("async-job")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    Step step1() {

        return steps.get("step1")
                .<Product, Future<Product>>chunk(5)
                .reader(itemReader)
                .processor(asyncProductProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    AsyncItemProcessor<Product, Product> asyncProductProcessor() {
        AsyncItemProcessor<Product, Product> processor = new AsyncItemProcessor<Product, Product>();
        processor.setDelegate(productProcessor());
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setMaxPoolSize(4);
//        taskExecutor.setCorePoolSize(4);
//        taskExecutor.afterPropertiesSet();
//        processor.setTaskExecutor(taskExecutor);
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor("product-thread"));
        return processor;
    }

    @Bean
    ItemProcessor<Product, Product> productProcessor() {
        return product -> {
            log.debug("Processing {}", product);
            Thread.sleep(fakeProcessorPause);
            return product;
        };
    }

    @Bean
    AsyncItemWriter<Product> asyncItemWriter() {
        AsyncItemWriter<Product> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(flatFileItemWriter(null));
        return asyncItemWriter;
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
