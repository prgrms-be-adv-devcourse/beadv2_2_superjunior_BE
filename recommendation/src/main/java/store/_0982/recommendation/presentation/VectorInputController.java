package store._0982.recommendation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.log.ControllerLog;
import store._0982.recommendation.application.VectorInputService;
import store._0982.recommendation.presentation.dto.ProductEmbeddingRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/recommendations")
public class VectorInputController {
    private final VectorInputService vectorInputService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/embedding")
    @ControllerLog
    public void embedding(@RequestBody ProductEmbeddingRequest request) {
        vectorInputService.embedding(request.size());
    }
}
