package net.shyshkin.study.itemreaders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@Data
@XmlRootElement(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long productID;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
