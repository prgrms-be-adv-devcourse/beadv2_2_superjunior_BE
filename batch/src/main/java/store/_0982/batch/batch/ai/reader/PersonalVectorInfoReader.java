package store._0982.batch.batch.ai.reader;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.ai.CartVector;
import store._0982.batch.domain.ai.OrderVector;
import store._0982.batch.infrastructure.client.commerce.CommerceFeignClient;
import store._0982.batch.infrastructure.client.member.MemberClient;
import store._0982.common.dto.ResponseDto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@StepScope
@Slf4j
public class PersonalVectorInfoReader implements ItemReader<PersonalVectorInfoReader.MemberVectorsInput> {

    private static final int PAGE_SIZE = 1_000;

    private final CommerceFeignClient commerceClient;
    private final MemberClient memberClient;

    private Iterator<UUID> memberIterator = List.<UUID>of().iterator();

    @PostConstruct
    void init() {
        this.memberIterator = fetchMemberIds().iterator();
    }

    @Override
    public MemberVectorsInput read() {
        while (memberIterator.hasNext()) {
            UUID memberId = memberIterator.next();
            List<CartVector> cartVectors = unwrap(commerceClient.getCarts(memberId));
            List<OrderVector> orderVectors = unwrap(commerceClient.getOrdersConsumer(memberId));
            log.info("카트 벡터 {}",cartVectors.get(0).getVector());
            if (!cartVectors.isEmpty() || !orderVectors.isEmpty()) {
                return new MemberVectorsInput(memberId, cartVectors, orderVectors);
            }
        }
        return null;
    }

    private List<UUID> fetchMemberIds() {
        List<UUID> ids = new ArrayList<>(PAGE_SIZE * 10);
        int page = 0;

        while (true) {
            ResponseDto<List<UUID>> response = memberClient.getMemberIds(page, PAGE_SIZE);
            List<UUID> batch = unwrap(response);
            if (batch.isEmpty()) {
                break;
            }

            ids.addAll(batch);
            if (batch.size() < PAGE_SIZE) {
                break;
            }
            page++;
        }
        //todo: id 내 중복 제거 필요
        return ids;
    }

    private <T> List<T> unwrap(ResponseDto<List<T>> response) {
        return response == null || response.data() == null ? List.of() : response.data();
    }

    public record MemberVectorsInput(
            UUID memberId,
            List<CartVector> cartVectors,
            List<OrderVector> orderVectors
    ) {
    }
}
