package store._0982.batch.batch.recommendation.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@StepScope
@Slf4j
public class PersonalVectorInfoReader implements ItemReader<PersonalVectorInfoReader.MemberVectorsInput> {

    private static final int PAGE_SIZE = 100;

    private static final String MEMBER_IDS_SQL = """
            select distinct member_id
            from order_schema."order"
            order by member_id
            limit ? offset ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private long offset = 0L;

    @Override
    public MemberVectorsInput read() {
        List<UUID> batch = jdbcTemplate.query(
                MEMBER_IDS_SQL,
                (rs, rowNum) -> rs.getObject("member_id", UUID.class),
                PAGE_SIZE,
                offset
        );
        if (batch.isEmpty()) {
            return null;
        }
        offset += PAGE_SIZE;
        return new MemberVectorsInput(batch);
    }

    public record MemberVectorsInput(
            List<UUID> memberIds
    ) {
    }
}
