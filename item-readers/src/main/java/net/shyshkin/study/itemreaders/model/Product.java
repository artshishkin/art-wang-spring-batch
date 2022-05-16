package net.shyshkin.study.itemreaders.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@Data
@XmlRootElement(name = "product")
public class Product {

    private Long productID;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
