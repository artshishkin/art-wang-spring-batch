package net.shyshkin.study.batch.performance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSql {

    private Long prodId;
    private String prodName;
    private String prodDesc;
    private BigDecimal price;
    private Long unit;

}
