package net.shyshkin.study.jpa.mapper;

import net.shyshkin.study.jpa.model.Category;
import net.shyshkin.study.jpa.model.Product;
import net.shyshkin.study.jpa.model.ProductOut;
import net.shyshkin.study.jpa.model.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ProductMapper {

    public ProductOut map(Product product, Category category) {
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

}
