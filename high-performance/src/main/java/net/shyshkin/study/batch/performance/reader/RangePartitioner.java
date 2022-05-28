package net.shyshkin.study.batch.performance.reader;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class RangePartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> result = new HashMap<>();

        int range = 10;
        int fromId = 1;
        int toId = range;

        for (int i = 1; i <= gridSize; i++) {
            var executionContext = new ExecutionContext();
            executionContext.put("minValue", fromId);
            executionContext.put("maxValue", toId);

            result.put("partition" + i, executionContext);
            fromId = toId + 1;
            toId += range;
        }

        return result;
    }
}
