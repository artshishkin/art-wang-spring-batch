package net.shyshkin.study.batch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.model.Product;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@MicronautTest
class ProductControllerTest {

    @Inject
    @Client("/products")
    HttpClient client;


    @Test
    void products_shouldReturnAppropriateStatusAndMediaType() {
        //given
        var expectedStatus = HttpStatus.OK;

        //when
        var response = client.toBlocking().exchange("/", JsonNode.class);

        //then
        assertEquals(expectedStatus, response.getStatus());
        MediaType mediaType = response.getContentType().orElseThrow();
        assertEquals(MediaType.APPLICATION_JSON, mediaType.getName());
    }

    @Test
    void products_shouldReturnCorrectContentAt0Position() {

        //given
        var expectedContent = "Apple WEB";

        //when
        var response = client.toBlocking().exchange("/", JsonNode.class);

        //then
        JsonNode body = response.body();
        log.info("{}", body.at("/0/productName"));
        assertEquals(expectedContent, body.at("/0/productName").textValue());
    }

    @Test
    void products_shouldReturnCorrectJSON() {

        //given
        var expectedContent = "[{\"productID\":1,\"productName\":\"Apple WEB\",\"productDesc\":\"apple cell phone\",\"price\":500.0,\"unit\":10},{\"productID\":2,\"productName\":\"Dell WEB\",\"productDesc\":\"dell computer\",\"price\":3000,\"unit\":5},{\"productID\":3,\"productName\":\"office WEB\",\"productDesc\":\"ms office software\",\"price\":196,\"unit\":23}]";

        //when
        var response = client.toBlocking().exchange("/", JsonNode.class);

        //then
        JsonNode body = response.body();
        log.debug("{}", body);
        assertEquals(expectedContent, body.toString());

    }

    @Test
    void products_shouldReturnAllProducts_exchange() {

        //given
        var expectedSize = 3;
        var expectedProductNames = new String[]{"Apple WEB", "Dell WEB", "office WEB"};

        //when
        var response = client.toBlocking().exchange("/", Product[].class);

        //then
        assertThat(response)
                .hasFieldOrPropertyWithValue("status", HttpStatus.OK)
                .satisfies(resp -> assertThat(resp.getContentType()).hasValue(MediaType.APPLICATION_JSON_TYPE))
                .satisfies(resp -> assertThat(resp.body())
                        .hasSize(expectedSize)
                        .extracting("productName")
                        .containsExactlyInAnyOrder(expectedProductNames)
                );
    }

    @Test
    void products_shouldReturnAllProducts_retrieve() {

        //given
        var expectedSize = 3;
        var expectedProductNames = new String[]{"Apple WEB", "Dell WEB", "office WEB"};

        //when
        var result = client.toBlocking().retrieve("/", List.class);

        //then
        final List<LinkedHashMap<String, String>> products = result;
        assertThat(products)
                .hasSize(expectedSize)
                .extracting(entry -> entry.get("productName"))
                .containsExactlyInAnyOrder(expectedProductNames);

    }

    @Test
    void getProduct_whenProductAbsent_shouldReturnNotFound() {
        //given
        long absentId = 1000L;
        var expectedStatus = HttpStatus.NOT_FOUND;

        //when
        ThrowableAssert.ThrowingCallable execution = () -> {
            var response = client.toBlocking().exchange("/" + absentId, JsonNode.class);
        };

        //then
        assertThatThrownBy(execution)
                .isInstanceOf(HttpClientResponseException.class)
                .hasMessage("Not Found")
                .extracting(t -> (HttpClientResponseException) t)
                .satisfies(ex -> assertAll(
                                () -> assertThat((CharSequence) ex.getStatus()).isEqualTo(expectedStatus),
                                () -> assertThat(ex.getResponse().body()).isNull()
                        )
                );
    }

    @Test
    void getProduct_shouldReturnServerError3of4times() {
        //given
        long id = 1L;

        //when
        for (int i = 0; i < 3; i++) {

            //when
            ThrowableAssert.ThrowingCallable execution = () -> {
                var response = client.toBlocking().exchange("/" + id, JsonNode.class);
            };

            //then
            assertThatThrownBy(execution)
                    .isInstanceOf(HttpClientResponseException.class)
                    .hasMessage("Internal Server Error")
                    .extracting(t -> (HttpClientResponseException) t)
                    .satisfies(ex -> assertAll(
                                    () -> assertThat((CharSequence) ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR),
                                    () -> assertThat(ex.getResponse().body()).isNull()
                            )
                    );
        }
        var response = client.toBlocking().exchange("/" + id, Product.class);
        //then
        assertThat((CharSequence) response.status()).isEqualTo(HttpStatus.OK);
        assertThat(response.body())
                .hasNoNullFieldsOrProperties();
        MediaType mediaType = response.getContentType().orElseThrow();
        assertEquals(MediaType.APPLICATION_JSON, mediaType.getName());
    }

}