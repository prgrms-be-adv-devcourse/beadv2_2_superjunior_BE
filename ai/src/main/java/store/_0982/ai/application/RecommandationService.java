package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.ai.application.dto.RecommandationInfo;
import store._0982.ai.application.dto.RecommandationSearchRequest;
import store._0982.ai.application.dto.RecommandationSearchResponse;
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

    private static final int NUM_OF_RECO = 8;

    private final SearchQueryPort searchQueryPort;
    private final PersonalVectorRepository personalVectorRepository;

    public List<RecommandationInfo> getRecommandations(UUID memberId, String keyword, String category) {
        PersonalVector personalVector = personalVectorRepository.findById(memberId).orElseThrow(()->new CustomException(CustomErrorCode.BAD_REQUEST));
        List<RecommandationSearchResponse> candidates = searchQueryPort.getRecommandationCandidates(new RecommandationSearchRequest(keyword, category, personalVector.getVector(), NUM_OF_RECO * 3));

        return new LinkedList<RecommandationInfo>();
    }

}
