package store._0982.batch.batch.recommendation.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.recommendation.reader.PersonalVectorInfoReader.MemberVectorsInput;
import store._0982.common.domain.vector.PersonalVector;
import store._0982.common.domain.vector.ProductVector;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonalVectorProcessor implements ItemProcessor<List<MemberVectorsInput>, List<PersonalVector>> {

    @Override
    public List<PersonalVector> process(List<MemberVectorsInput> item) {
        if (item == null || item.isEmpty()) {
            return List.of();
        }
        List<PersonalVector> results = new ArrayList<>(item.size());
        for (MemberVectorsInput input : item) {
            if (input == null || input.memberId() == null) {
                continue;
            }
            List<ProductVector> productVectors = input.productVectors();
            if (productVectors == null || productVectors.isEmpty()) {
                continue;
            }
            List<float[]> vectors = new ArrayList<>(productVectors.size());
            for (ProductVector productVector : productVectors) {
                if (productVector == null || productVector.getVector() == null) {
                    continue;
                }
                vectors.add(productVector.getVector());
            }
            if (vectors.isEmpty()) {
                continue;
            }
            float[] averageVector = VectorUtil.getAverageVector(vectors);
            results.add(PersonalVector.create(input.memberId(), averageVector));
        }
        return results;
    }
}
