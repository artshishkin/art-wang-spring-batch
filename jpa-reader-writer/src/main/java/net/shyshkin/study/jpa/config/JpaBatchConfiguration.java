package net.shyshkin.study.jpa.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.jpa.model.Category;
import net.shyshkin.study.jpa.model.Product;
import net.shyshkin.study.jpa.model.ProductOut;
import net.shyshkin.study.jpa.model.Review;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class JpaBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final EntityManagerFactory factory;

    @Bean
    public Job jpaJob() {
        return jobs.get("jpaJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaStep())
                .build();
    }

    @Bean
    Step jpaStep() {
        return steps.get("readJpa")
                .<Category, List<ProductOut>>chunk(3)
                .reader(reader())
                .processor(productProcessor())
                .writer(items -> items.forEach(item -> log.debug("{}", item)))
                .build();
    }

    @Bean
    ItemProcessor<Category, List<ProductOut>> productProcessor() {
        return category -> category.getProducts()
                .stream()
                .map(product -> mapProduct(product, category))
                .collect(Collectors.toList());
    }

    private ProductOut mapProduct(Product product, Category category) {
        ProductOut productOut = ProductOut.builder()
                .name(product.getProductName())
                .description(product.getProductDesc())
                .category(category)
                .price(product.getPrice())
                .unit(product.getUnit())
                .build();

        List<Review> reviews = IntStream.rangeClosed(1, 3).boxed()
                .map(i -> Review.builder()
                        .author("Author " + i)
                        .content("Content " + product.getProductName() + " " + i)
                        .build())
                .collect(Collectors.toList());

        productOut.setReviews(reviews);
        return productOut;
    }


    @Bean
    JpaCursorItemReader<Category> reader() {
        return new JpaCursorItemReaderBuilder<Category>()
                .name("jpaReader")
                .queryString("select c from Category c")
                .entityManagerFactory(factory)
                .build();
    }


}
