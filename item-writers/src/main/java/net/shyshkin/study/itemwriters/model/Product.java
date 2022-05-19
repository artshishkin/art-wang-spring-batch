package net.shyshkin.study.itemwriters.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@XmlRootElement(name = "product") //JAXB
public class Product {

    private Long productID;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
