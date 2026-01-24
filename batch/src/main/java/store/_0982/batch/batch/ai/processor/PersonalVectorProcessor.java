package store._0982.batch.batch.ai.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.ai.reader.PersonalVectorInfoReader.MemberVectorsInput;
import store._0982.batch.domain.ai.PersonalVector;
import store._0982.batch.domain.ai.ProductVector;
import store._0982.batch.domain.ai.VectorUtil;
import store._0982.batch.infrastructure.client.ai.AiFeignClient;
import store._0982.batch.infrastructure.client.ai.dto.InterestSummaryRequest;

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonalVectorProcessor implements ItemProcessor<MemberVectorsInput, PersonalVector> {

    private final AiFeignClient aiClient;

    @Override
    public PersonalVector process(MemberVectorsInput item) {
        List<String> descriptions = new LinkedList<>();
        descriptions.addAll(item.cartVectors().stream().map(ProductVector::getDescription).toList());
        descriptions.addAll(item.orderVectors().stream().map(ProductVector::getDescription).toList());
        String interestSummary = aiClient.summarizeInterest(new InterestSummaryRequest(descriptions));


        return PersonalVector.create(
                item.memberId(),
                VectorUtil.getAverageVector(item.cartVectors(), item.orderVectors()),
                interestSummary
        );
    }
}
