package net.shyshkin.study.itemreaders.containers;

import net.shyshkin.study.itemreaders.AbstractJobTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "logging.level.net.shyshkin=debug",
        "app.external.product-service.url=http://${SERVICES_HOST}:${SERVICES_PORT}"
})
@ContextConfiguration(initializers = ProductServiceAbstractTest.Initializer.class)
public abstract class ProductServiceAbstractTest extends AbstractJobTest {

    @Container
    static ProductServiceContainers productServiceContainers = ProductServiceContainers.getInstance();

    protected static GenericContainer<?> productServiceContainer = productServiceContainers.getProductService();

    protected static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String host = productServiceContainer.getHost();
            Integer port = productServiceContainer.getMappedPort(8090);

            System.setProperty("SERVICES_HOST", host);
            System.setProperty("SERVICES_PORT", String.valueOf(port));
        }
    }

}