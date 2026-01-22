package store._0982.batch.batch.ai.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.ai.reader.PersonalVectorInfoReader.MemberVectorsInput;
import store._0982.batch.domain.ai.PersonalVector;
import store._0982.batch.domain.ai.VectorUtil;

@Component
public class PersonalVectorProcessor implements ItemProcessor<MemberVectorsInput, PersonalVector> {

    @Override
    public PersonalVector process(MemberVectorsInput item) {
        return PersonalVector.create(
                item.memberId(),
                VectorUtil.getAverageVector(item.cartVectors(), item.orderVectors())
        );
    }
}
