package net.shyshkin.study.batch.service;

import jakarta.inject.Singleton;
import net.shyshkin.study.batch.exception.EntityNotFoundException;
import net.shyshkin.study.batch.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Singleton
public class ProductService {

    private final List<Product> products = List.of(
            Product.builder()
                    .productID(1L)
                    .productName("Apple WEB")
                    .productDesc("apple cell phone")
                    .price(new BigDecimal("500.00"))
                    .unit(10L)
                    .build(),
            Product.builder()
                    .productID(2L)
                    .productName("Dell WEB")
                    .productDesc("dell computer")
                    .price(new BigDecimal("3000"))
                    .unit(5L)
                    .build(),
            Product.builder()
                    .productID(3L)
                    .productName("office WEB")
                    .productDesc("ms office software")
                    .price(new BigDecimal("196"))
                    .unit(23L)
                    .build()
    );

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProduct(Long id) {
        return this.products
                .stream()
                .filter(pr -> Objects.equals(pr.getProductID(), id))
                .findAny()
                .orElseThrow(EntityNotFoundException::new);
    }

}
