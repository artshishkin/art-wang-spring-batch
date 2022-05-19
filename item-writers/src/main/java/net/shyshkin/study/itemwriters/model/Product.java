package net.shyshkin.study.itemwriters.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
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

    @XStreamAlias("prodId")
    private Long productID;
    @XStreamAlias("prodName")
    private String productName;
    @XStreamAlias("prodDescription")
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
