package net.shyshkin.study.jpa.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class IterableItemWriter<T> implements ItemWriter<Iterable<T>> {

    private final ItemWriter<T> delegate;

    @Override
    public void write(List<? extends Iterable<T>> items) throws Exception {
        for (Iterable<T> itemIter : items) {
            List<T> list = StreamSupport.stream(itemIter.spliterator(), false)
                    .collect(Collectors.toList());
            delegate.write(list);
        }
    }
}
