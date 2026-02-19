package store._0982.recommendation.application;

import store._0982.recommendation.application.dto.RecommendInfo;

import java.util.UUID;

interface CacheService {

    RecommendInfo getRecommendationList(UUID memberId);

    Long getTtlOfKey(UUID memberId);

    void putRecommendationList(UUID memberId, RecommendInfo recommendInfo);

    Long TTL_SECONDS = 24 * 60 * 60 * 7L; // 일주일
}
