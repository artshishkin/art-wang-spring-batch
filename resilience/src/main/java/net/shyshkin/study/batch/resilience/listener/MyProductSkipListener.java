package net.shyshkin.study.batch.resilience.listener;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.resilience.model.Product;
import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
public class MyProductSkipListener extends SkipListenerSupport<Product,Product> {

    @Value("${app.error.skip.file:output/resilience/error_skipped.txt}")
    private Path errorFile;

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(errorFile.getParent());
        } catch (IOException e) {
            log.debug("Error: {}", e.getMessage());
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException) {
            FlatFileParseException ex = (FlatFileParseException) t;
            onSkip(ex.getInput());
        }
    }

    private void onSkip(String input) {

        try {
            Files.newBufferedWriter(errorFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                    .append(input)
                    .append("\r\n")
                    .flush();
        } catch (IOException e) {
            log.debug("Error: {}", e.getMessage());
        }
    }



}
