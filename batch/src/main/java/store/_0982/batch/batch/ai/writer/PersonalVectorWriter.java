package store._0982.batch.batch.ai.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.ai.PersonalVector;
import store._0982.batch.domain.ai.PersonalVectorRepository;

@Component
@RequiredArgsConstructor
public class PersonalVectorWriter implements ItemWriter<PersonalVector> {

    private final PersonalVectorRepository personalVectorRepository;

    @Override
    public void write(Chunk<? extends PersonalVector> chunk) {
        personalVectorRepository.saveAll(chunk.getItems()); // Id 충돌 시 자동 update
    }
}
