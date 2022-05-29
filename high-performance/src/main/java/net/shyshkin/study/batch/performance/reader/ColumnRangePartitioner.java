package net.shyshkin.study.batch.performance.reader;

import lombok.Builder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class ColumnRangePartitioner implements Partitioner {

    private final JdbcOperations jdbcTemplate;
    private final String table;
    private final String column;

    @Builder
    public ColumnRangePartitioner(DataSource dataSource, String table, String column) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.table = table;
        this.column = column;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        String query = String.format("select min(%s) from %s ", this.column, this.table);
        Long minValue = jdbcTemplate.queryForObject(query, Long.class);
        query = String.format("select max(%s) from %s ", this.column, this.table);
        Long maxValue = jdbcTemplate.queryForObject(query, Long.class);

        long targetSize = (maxValue - minValue) / gridSize + 1;
        Map<String, ExecutionContext> result = new HashMap<>();

        long start = minValue;
        long end = start + targetSize - 1;

        for (int i = 1; i <= gridSize; i++) {
            var executionContext = new ExecutionContext();
            executionContext.put("minValue", start);
            executionContext.put("maxValue", end);

            result.put("partition" + i, executionContext);
            start = end + 1;
            end += targetSize;
        }

        return result;
    }
}
