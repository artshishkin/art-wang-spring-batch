package net.shyshkin.study.batch.resilience.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.exception.Service404Exception;
import net.shyshkin.study.batch.resilience.exception.Service500Exception;
import net.shyshkin.study.batch.resilience.exception.ServiceWrongStatusException;
import net.shyshkin.study.batch.resilience.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.external.product-service.url:http://locahost:8090}")
    private URI serverUrl;

    public Product getProduct(Long id) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(serverUrl.resolve("/products/" + id))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200:
                return objectMapper.readValue(response.body(), Product.class);
            case 500:
                throw new Service500Exception();
            case 404:
                throw new Service404Exception();
            default:
                throw new ServiceWrongStatusException();
        }
    }
}
