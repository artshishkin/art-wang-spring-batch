package net.shyshkin.study.itemreaders.service;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemreaders.model.Product;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false"
})
@Disabled("Only for manual testing. Start product-service first (docker-compose or jar)")
class ProductClientManualTest {

    @Autowired
    ProductClient productClient;

    @Test
    void getAllProducts() throws IOException, InterruptedException {

        //when
        List<Product> products = productClient.getAllProducts();

        //then
        assertThat(products)
                .hasSize(3)
                .allSatisfy(product -> assertAll(
                        () -> assertThat(product).hasNoNullFieldsOrProperties(),
                        () -> assertThat(product.getProductID()).isGreaterThan(0),
                        () -> assertThat(product.getPrice()).isGreaterThan(new BigDecimal("0.0")),
                        () -> assertThat(product.getProductName()).isNotEmpty(),
                        () -> assertThat(product.getProductDesc()).isNotEmpty(),
                        () -> log.debug("{}", product)
                ));
    }
}