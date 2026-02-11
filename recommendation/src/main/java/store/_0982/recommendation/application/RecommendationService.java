package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.domain.vector.PersonalVector;
import store._0982.common.domain.vector.ProductVector;
import store._0982.recommendation.application.dto.*;
import store._0982.recommendation.domain.PersonalVectorRepository;
import store._0982.recommendation.domain.ProductVectorRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int NUM_OF_RECO = 3;

    private final SearchQueryPort searchQueryPort;
    private final PersonalVectorRepository personalVectorRepository;
    private final PromptService promptService ;
    private final ProductVectorRepository productVectorRepository;

    public RecommandInfo getRecommendations(UUID memberId) {
        PersonalVector personalVector = personalVectorRepository.findById(memberId).orElse(null);
        if(personalVector == null) return null;
        List<VectorSearchResponse> candidates = searchQueryPort.getRecommandationCandidates(
                new VectorSearchRequest(personalVector.getVector(), NUM_OF_RECO * 2)
        );
        List<GroupPurchase> groupPurchases = candidates.stream().map(GroupPurchase::from).toList();

        LlmResponse llmResponse = promptService.askToChatModel(
                groupPurchases.stream().map(SimpleGroupPurchaseInfo::from).toList(),
                "",
                NUM_OF_RECO
        );

        List<GroupPurchase> recommendedGpList = convertLlmResponseToGp(llmResponse, groupPurchases);

        return new RecommandInfo(recommendedGpList, llmResponse.reason());
    }

    private List<GroupPurchase> convertLlmResponseToGp(LlmResponse llmResponse, List<GroupPurchase> groupPurchaseList) {

        List<GroupPurchase> resultInfos = new LinkedList<>();
        for(LlmResponse.GroupPurchase gp : llmResponse.groupPurchases()) {
            for(GroupPurchase groupPurchase : groupPurchaseList) {
                if(groupPurchase.groupPurchaseId().equals(gp.groupPurchaseId())){
                    resultInfos.add(groupPurchase);
                }
            }
        }
        return resultInfos;
    }

    public float[] getProductVector(UUID productId) {
        return productVectorRepository.findById(productId)
                .map(ProductVector::getVector)
                .orElse(null);
    }
}
