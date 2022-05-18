package net.shyshkin.study.itemreaders.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemreaders.model.Product;
import net.shyshkin.study.itemreaders.service.ProductClientAdapter;
import net.shyshkin.study.itemreaders.writer.ConsoleItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class WebServiceReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;
    private final ProductClientAdapter productClientAdapter;

    @Bean
    public Job webServiceReadJob() {
        return jobs.get("webServiceReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readWebServiceStep())
                .build();
    }

    @Bean
    Step readWebServiceStep() {
        return steps.get("readWebService")
                .<Product, Product>chunk(3)
                .reader(webServiceReader())
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    ItemReaderAdapter<Product> webServiceReader() {

        ItemReaderAdapter<Product> readerAdapter = new ItemReaderAdapter<>();

        readerAdapter.setTargetObject(productClientAdapter);
        readerAdapter.setTargetMethod("getProduct");
        return readerAdapter;
    }
}
