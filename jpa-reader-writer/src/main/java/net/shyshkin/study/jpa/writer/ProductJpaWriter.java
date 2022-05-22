package net.shyshkin.study.jpa.writer;

import net.shyshkin.study.jpa.model.ProductOut;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.util.List;

@Component
public class ProductJpaWriter implements ItemWriter<List<ProductOut>> {

    private final ItemWriter<ProductOut> jpaItemWriter;

    public ProductJpaWriter(EntityManagerFactory factory) {
        jpaItemWriter = new JpaItemWriterBuilder<ProductOut>()
                .entityManagerFactory(factory)
                .usePersist(false)
                .build();
    }

    @Transactional
    @Override
    public void write(List<? extends List<ProductOut>> products) throws Exception {
        for (List<ProductOut> prodList : products) {
            jpaItemWriter.write(prodList);
        }
    }
}
