package net.shyshkin.study.helloworld.writer;

import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

import java.util.List;

public class ConsoleItemWriter extends AbstractItemStreamItemWriter<Integer> {

    @Override
    public void write(List<? extends Integer> items) throws Exception {
        System.out.println("******* writing each chunk *******");
        items.forEach(System.out::println);
    }
}
