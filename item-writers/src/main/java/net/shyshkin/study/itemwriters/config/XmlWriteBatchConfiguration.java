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
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.Map;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class XmlWriteBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final FlatFileItemReader<Product> itemReader;

    @Bean
    public Job xmlWriteJob() {
        return jobs.get("xmlWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(writeXmlStep())
                .build();
    }

    @Bean
    Step writeXmlStep() {
        return steps.get("writeXml")
                .<Product, Product>chunk(3)
                .reader(itemReader)
                .writer(xmlItemWriter(null))
                .build();
    }

    @Bean
    @StepScope
    StaxEventItemWriter<Product> xmlItemWriter(@Value("#{jobParameters['xmlOutputFile']}") FileSystemResource outputFile) {
        var marshaller = new XStreamMarshaller();
        marshaller.setAliases(Map.of("product", Product.class));

        return new StaxEventItemWriterBuilder<Product>()
                .name("xmlProductWriter")
                .resource(outputFile)
                .marshaller(marshaller)
                .rootTagName("products")
                .build();
    }

}
