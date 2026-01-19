package store._0982.batch.batch.ai.reader;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.batch.application.commerce.CommerceQueryPort;
import store._0982.batch.domain.ai.CartVector;
import store._0982.batch.domain.ai.OrderVector;

import java.util.List;
import java.util.UUID;

@Component
@StepScope
public class PersonalVectorInfoReader implements ItemReader<PersonalVectorInfoReader.MemberVectorsInput> {

    private final CommerceQueryPort commerceQueryPort;
    private final UUID memberId;
    private boolean consumed = false;

    public PersonalVectorInfoReader(
            CommerceQueryPort commerceQueryPort,
            @Value("#{jobParameters['memberId']}") String memberId
    ) {
        this.commerceQueryPort = commerceQueryPort;
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("memberId job parameter is required for vectorRefreshJob");
        }
        this.memberId = UUID.fromString(memberId);
    }

    @Override
    public MemberVectorsInput read() {
        if (consumed) {
            return null;
        }

        List<CartVector> cartVectors = commerceQueryPort.getCarts(memberId);
        List<OrderVector> orderVectors = commerceQueryPort.getOrders(memberId);
        consumed = true;
        return new MemberVectorsInput(memberId, cartVectors, orderVectors);
    }

    public record MemberVectorsInput(
            UUID memberId,
            List<CartVector> cartVectors,
            List<OrderVector> orderVectors
    ) {
    }
}
