package net.shyshkin.study.itemwriters.processor;

import net.shyshkin.study.itemwriters.model.Product;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ProductProcessor implements ItemProcessor<Product, Product> {

    @Override
    public Product process(Product product) throws Exception {
        if (product.getProductID() == 2) return null;
        var builder = new StringBuilder(product.getProductDesc());
        product.setProductDesc(builder.reverse().toString());
        product.setProductName(product.getProductName().toUpperCase().replaceAll("\\s*", "_"));
        return product;
    }
}
