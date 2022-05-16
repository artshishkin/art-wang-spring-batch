package net.shyshkin.study.itemreaders.writer;

import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

import java.util.List;

public class ConsoleItemWriter<T> extends AbstractItemStreamItemWriter<T> {

    @Override
    public void write(List<? extends T> items) throws Exception {
        System.out.println("******* writing each chunk *******");
        items.forEach(System.out::println);
    }
}
