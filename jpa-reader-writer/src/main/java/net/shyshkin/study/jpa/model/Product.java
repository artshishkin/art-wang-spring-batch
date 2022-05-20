package net.shyshkin.study.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products_jpa")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_id")
    private Integer productID;
    @Column(name = "prod_name")
    private String productName;
    @Column(name = "prod_desc")
    private String productDesc;
    private BigDecimal price;
    private Long unit;

}
