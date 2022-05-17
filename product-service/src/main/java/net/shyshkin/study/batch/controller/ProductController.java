package net.shyshkin.study.batch.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.RequiredArgsConstructor;
import net.shyshkin.study.batch.model.Product;
import net.shyshkin.study.batch.service.ProductService;

import java.util.List;

@Controller("products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Get
    public List<Product> products() {
        return productService.getAllProducts();
    }
}
