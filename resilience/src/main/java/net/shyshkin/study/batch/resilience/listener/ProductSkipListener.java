package net.shyshkin.study.batch.resilience.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.OnSkipInRead;
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
public class ProductSkipListener {

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

    @OnSkipInRead
    public void onSkipRead(Throwable t) {

        if (t instanceof FlatFileParseException) {
            FlatFileParseException ex = (FlatFileParseException) t;
            onSkip(ex.getInput());
        }
    }

    private void onSkip(Object o) {

        try {
//            Files.writeString(errorFile, o.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.newBufferedWriter(errorFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                    .append(o.toString())
                    .append("\r\n")
                    .flush();
        } catch (IOException e) {
            log.debug("Error: {}", e.getMessage());
        }
    }

}
