package net.shyshkin.study.batch.performance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.model.Product;
import net.shyshkin.study.batch.performance.model.ProductSql;
import net.shyshkin.study.batch.performance.reader.ColumnRangePartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.Map;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("column-range-partitioner")
public class ColumnRangePartitionerBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final DataSource dataSource;

    @Value("${app.processor.pause}")
    private Long fakeProcessorPause;

    @Bean
    public Job rangePartitionerJob() {
        return jobs.get("column-range-partitioner-job")
                .incrementer(new RunIdIncrementer())
                .start(partitionStep())
                .build();
    }

    @Bean
    Step partitionStep() {
        return steps.get("partitionStep")
                .partitioner(
                        slaveStep().getName(),
                        ColumnRangePartitioner.builder()
                                .dataSource(dataSource)
                                .table("products")
                                .column("prod_id")
                                .build()
                )
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    Step slaveStep() {
        return steps.get("slaveStep")
                .<ProductSql, Product>chunk(5)
                .reader(pagingItemReader(null, null))
                .processor(productProcessor())
                .writer(consoleWriter())
                .build();
    }

    @Bean
    @StepScope
    ItemReader<ProductSql> pagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        return new JdbcPagingItemReaderBuilder<ProductSql>()
                .name("pagingProductReader")
                .dataSource(dataSource)
                .pageSize(1000)
                .sortKeys(Map.of("prod_id", Order.ASCENDING))
                .selectClause("prod_id, prod_name, prod_desc, price, unit")
                .fromClause("from products")
                .whereClause(String.format("where prod_id >= %d and prod_id <= %d", minValue, maxValue))
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
    ItemWriter<Product> consoleWriter() {
        return items -> items.forEach(item -> log.debug("{}", item));
    }


}
