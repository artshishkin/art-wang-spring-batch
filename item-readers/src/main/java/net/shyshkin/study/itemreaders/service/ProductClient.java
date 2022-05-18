package net.shyshkin.study.itemreaders.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.shyshkin.study.itemreaders.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.external.product-service.url}")
    private URI serverUrl;

    public List<Product> getAllProducts() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(serverUrl.resolve("/products"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), new TypeReference<List<Product>>() {
        });
    }
}
