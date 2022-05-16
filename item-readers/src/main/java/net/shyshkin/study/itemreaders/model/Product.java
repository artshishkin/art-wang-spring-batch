package net.shyshkin.study.itemreaders.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Product {

    private Long productID;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
