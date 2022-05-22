package net.shyshkin.study.batch.resilience.processor;

import net.shyshkin.study.batch.resilience.model.Product;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ProductProcessor implements ItemProcessor<Product, Product> {

    @Override
    public Product process(Product product) throws Exception {
        if (product.getProductID() == 2)
            throw new RuntimeException("Because id is 2");
        var builder = new StringBuilder(product.getProductDesc());
        product.setProductDesc(builder.reverse().toString());
        product.setProductName(product.getProductName().toUpperCase().replaceAll("\\s*", "_"));
        return product;
    }
}
