package net.shyshkin.study.helloworld.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InMemItemProcessor implements ItemProcessor<Integer, Integer> {
    @Override
    public Integer process(Integer item) throws Exception {
        return item + 10;
    }
}
