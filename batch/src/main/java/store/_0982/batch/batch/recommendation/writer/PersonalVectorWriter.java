package store._0982.batch.batch.recommendation.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.ai.PersonalVector;
import store._0982.batch.domain.ai.PersonalVectorRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonalVectorWriter implements ItemWriter<List<PersonalVector>> {

    private final PersonalVectorRepository personalVectorRepository;

    @Override
    public void write(Chunk<? extends List<PersonalVector>> chunk) {
        List<PersonalVector> toSave = new ArrayList<>();
        for (List<PersonalVector> batch : chunk.getItems()) {
            if (batch == null || batch.isEmpty()) {
                continue;
            }
            toSave.addAll(batch);
        }
        if (toSave.isEmpty()) {
            return;
        }
        personalVectorRepository.saveAll(toSave); // Id 충돌 시 자동 update
    }
}
