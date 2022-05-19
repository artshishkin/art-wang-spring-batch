package itemwriters.config;

import itemwriters.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfiguration {

    @Bean
    @StepScope
    FlatFileItemReader<Product> flatFileItemReader(@Value("#{jobParameters['csvInputFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productCsvReader")
                .resource(inputFile)
                .linesToSkip(1)
                .delimited()
                .names("productID", "productName", "productDesc", "price", "unit")
                .targetType(Product.class)
                .build();
    }
}
