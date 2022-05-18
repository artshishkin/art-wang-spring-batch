package net.shyshkin.study.itemreaders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.itemreaders.model.Product;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductClientAdapter {

    private final ProductClient productClient;
    private Deque<Product> products;

    @PostConstruct
    void init() {
        products = new LinkedList<>();
        try {
            products.addAll(productClient.getAllProducts());
        } catch (IOException | InterruptedException e) {
            log.warn("Error occurred {}", e.getMessage());
        }
    }

    public Product getProduct() {
        return products.poll();
    }

}
