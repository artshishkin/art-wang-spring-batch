package net.shyshkin.study.helloworld.reader;

import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InMemReader extends AbstractItemStreamItemReader<Integer> {

    private final LinkedList<Integer> myList = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toCollection(LinkedList::new));

    @Override
    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return myList.poll();
    }
}
