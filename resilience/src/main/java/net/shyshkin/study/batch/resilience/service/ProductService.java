package net.shyshkin.study.batch.resilience.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.exception.ProductServiceException;
import net.shyshkin.study.batch.resilience.model.Product;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductClient productClient;

    public Product getProduct() throws InterruptedException {

        Product product = null;
        log.debug("Getting Product from external service...");
        try {
            product = productClient.getProduct(1L);
            log.debug("Reading product: {}", product);
        } catch (IOException e) {
            log.debug("Exception... {}", e.getMessage());
            throw new ProductServiceException(e);
        }
        return product;
    }
}
