package net.shyshkin.study.batch.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import net.shyshkin.study.batch.model.Product;
import net.shyshkin.study.batch.service.ProductService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Controller("products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @Get
    public List<Product> products() {
        return productService.getAllProducts();
    }

    @Get(value = "{id}")
    public MutableHttpResponse<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return requestCounter.incrementAndGet() % 4 != 0 ?
                HttpResponse.serverError() :
                HttpResponse.ok(product);
    }

}
