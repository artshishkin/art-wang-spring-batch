package net.shyshkin.study.batch.performance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.model.Product;
import net.shyshkin.study.batch.performance.model.ProductSql;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("range-partitioner")
public class RangePartitionerBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final DataSource dataSource;

    @Value("${app.processor.pause}")
    private Long fakeProcessorPause;

    @Bean
    public Job rangePartitionerJob() {
        return jobs.get("range-partitioner-job")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    Step step1() {

        return steps.get("step1")
                .<ProductSql, Product>chunk(5)
                .reader(itemReader())
                .processor(productProcessor())
                .writer(flatFileItemWriter(null))
                .build();
    }

    @Bean
    ItemReader<ProductSql> itemReader() {
        return new JdbcPagingItemReaderBuilder<ProductSql>()
                .name("pagingProductReader")
                .dataSource(dataSource)
                .pageSize(10)
                .sortKeys(Map.of("prod_id", Order.ASCENDING))
                .selectClause("prod_id, prod_name, prod_desc, price, unit")
                .fromClause("from products")
                .beanRowMapper(ProductSql.class)
                .build();
    }

    @Bean
    ItemProcessor<ProductSql, Product> productProcessor() {
        return productSql -> {
            log.debug("Processing {}", productSql);
            Thread.sleep(fakeProcessorPause);
            return Product.builder()
                    .productID(productSql.getProdId())
                    .productDesc(productSql.getProdDesc())
                    .productName(productSql.getProdName())
                    .price(productSql.getPrice())
                    .unit(productSql.getUnit())
                    .build();
        };
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
