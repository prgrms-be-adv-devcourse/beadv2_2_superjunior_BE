package store._0982.recommendation.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
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
public class RecommendationService {

    private static final int NUM_OF_RECO = 3;

    private final SearchQueryPort searchQueryPort;
    private final PersonalVectorRepository personalVectorRepository;
    private final PromptService promptService ;
    private final ProductVectorRepository productVectorRepository;
    private final CacheService cacheService;
    private final TaskExecutor taskExecutor;

    public RecommendationService(
            SearchQueryPort searchQueryPort,
            PersonalVectorRepository personalVectorRepository,
            PromptService promptService,
            ProductVectorRepository productVectorRepository,
            CacheService cacheService,
            @Qualifier("recommendationTaskExecutor") TaskExecutor taskExecutor
    ) {
        this.searchQueryPort = searchQueryPort;
        this.personalVectorRepository = personalVectorRepository;
        this.promptService = promptService;
        this.productVectorRepository = productVectorRepository;
        this.cacheService = cacheService;
        this.taskExecutor = taskExecutor;
    }



    public RecommendInfo getRecommendations(UUID memberId, String keyword, String category) {
        //캐시 체크 후 바로 리턴 + 비동기로 캐시 갱신 호출
        long ttl = cacheService.getTtlOfKey(memberId);
        if(ttl > 0) {
            RecommendInfo recommendInfo = cacheService.getRecommendationList(memberId);
            if(ttl < cacheService.TTL_SECONDS - 24 * 60 * 60) { //비동기로 캐시 갱신
                putCacheAsync(memberId, recommendInfo);
            }
            return recommendInfo;
        }

        RecommendInfo recommendInfo = getRecommendationThroughLlm(memberId, keyword, category);
        //캐시에 저장
        putCacheAsync(memberId, recommendInfo);
        return recommendInfo;
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

    private RecommendInfo getRecommendationThroughLlm(UUID memberId, String keyword, String category) {
        PersonalVector personalVector = personalVectorRepository.findById(memberId).orElse(null);
        if(personalVector == null) return null;
        List<VectorSearchResponse> candidates = searchQueryPort.getRecommandationCandidates(new VectorSearchRequest(keyword, category, personalVector.getVector(), NUM_OF_RECO * 2));
        List<GroupPurchase> groupPurchases = candidates.stream().map(GroupPurchase::from).toList();

        LlmResponse llmResponse = promptService.askToChatModel(keyword, category, groupPurchases.stream().map(SimpleGroupPurchaseInfo::from).toList(), "", NUM_OF_RECO);

        List<GroupPurchase> recommendedGpList = convertLlmResponseToGp(llmResponse, groupPurchases);

        return new RecommendInfo(recommendedGpList, llmResponse.reason());
    }

    private void putCacheAsync(UUID memberId, RecommendInfo info) {
        if (info == null) return;
        taskExecutor.execute(() -> cacheService.putRecommendationList(memberId, info));
    }

}
