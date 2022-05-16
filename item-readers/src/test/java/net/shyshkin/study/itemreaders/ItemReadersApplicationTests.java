package net.shyshkin.study.itemreaders;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
                "app.csv.file-path.product=../input/product.csv"
        }
)
class ItemReadersApplicationTests {

    @Test
    void contextLoads() {
    }

}
