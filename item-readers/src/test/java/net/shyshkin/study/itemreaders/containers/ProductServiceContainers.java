package net.shyshkin.study.itemreaders.containers;

import lombok.Getter;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Getter
public class ProductServiceContainers extends GenericContainer<ProductServiceContainers> {

    private static ProductServiceContainers instance;

    private static boolean containerStarted = false;

    private final GenericContainer<?> productService = new GenericContainer<>("artarkatesoft/art-wang-product-service:native")
            .withExposedPorts(8090)
            .waitingFor(Wait.forLogMessage(".*io.micronaut.runtime.Micronaut - Startup completed in.*\\n", 1));

    @Override
    public void start() {
        if (!containerStarted) {
            productService.start();
        }
        containerStarted = true;
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }

    public static ProductServiceContainers getInstance() {
        if (instance == null)
            instance = new ProductServiceContainers();
        return instance;
    }

}
