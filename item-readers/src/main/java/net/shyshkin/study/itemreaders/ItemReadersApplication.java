package net.shyshkin.study.itemreaders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ItemReadersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemReadersApplication.class, args);
    }

}
