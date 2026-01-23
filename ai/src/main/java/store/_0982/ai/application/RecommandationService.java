package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.ai.application.dto.*;
import store._0982.ai.domain.PersonalVector;
import store._0982.ai.domain.PersonalVectorRepository;
import store._0982.ai.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommandationService {

    private static final int NUM_OF_RECO = 3;

    private final SearchQueryPort searchQueryPort;
    private final PersonalVectorRepository personalVectorRepository;
    private final PromptService promptService ;

    public RecommandInfo getRecommandations(UUID memberId, String keyword, String category) {
        PersonalVector personalVector = personalVectorRepository.findById(memberId).orElseThrow(()->new CustomException(CustomErrorCode.BAD_REQUEST));
        List<VectorSearchResponse> candidates = searchQueryPort.getRecommandationCandidates(new VectorSearchRequest(keyword, category, personalVector.getVector(), NUM_OF_RECO * 2));
        List<GroupPurchase> groupPurchases = candidates.stream().map(GroupPurchase::from).toList();

        LlmResponse llmResponse = promptService.askToChatModel(keyword, category, groupPurchases.stream().map(SimpleGroupPurchaseInfo::from).toList(), personalVector.getInterestSummary(), NUM_OF_RECO);

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
}
