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
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class XmlReadBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final ConsoleItemWriter<Product> writer;

    @Bean
    public Job xmlReadJob() {
        return jobs.get("xmlReadJob")
                .incrementer(new RunIdIncrementer())
                .start(readXmlStep())
                .build();
    }

    @Bean
    Step readXmlStep() {
        return steps.get("readXml")
                .<Product, Product>chunk(3)
                .reader(xmlReader(null))
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    StaxEventItemReader<Product> xmlReader(@Value("#{jobParameters['xmlInputFile']}") FileSystemResource inputFile) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Product.class);
        return new StaxEventItemReaderBuilder<Product>()
                .name("productXmlReader")
                .resource(inputFile)
                .addFragmentRootElements("product")
                .unmarshaller(unmarshaller)
                .build();
    }

}
