package net.shyshkin.study.batch.model;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class Product {

    private Long productID;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
