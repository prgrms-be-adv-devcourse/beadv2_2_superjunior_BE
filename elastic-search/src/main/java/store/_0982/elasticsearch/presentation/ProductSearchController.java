package store._0982.elasticsearch.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.ProductSearchService;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;

import java.util.UUID;

@Tag(name = "Product Search", description = "상품 검색 및 색인")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/searches/product")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @Operation(summary = "상품 인덱스 생성", description = "상품 인덱스 생성.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PutMapping("/index")
    public ResponseDto<Void> createProductIndex() {
        productSearchService.createProductIndex();
        return new ResponseDto<>(HttpStatus.CREATED, null, "인덱스 생성 완료");
    }

    @Operation(summary = "상품 인덱스 삭제", description = "상품 인덱스를 삭제한다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @DeleteMapping("/index")
    public ResponseDto<Void> deleteProductIndex() {
        productSearchService.deleteProductIndex();
        return new ResponseDto<>(HttpStatus.OK, null, "인덱스 삭제 완료");
    }

    @Operation(summary = "상품 문서 검색", description = "키워드(제목, 설명) + sellerId 기준으로 상품을 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search")
    public ResponseDto<PageResponse<ProductDocumentInfo>> searchProductDocument(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam UUID sellerId,
            @RequestParam(required = false) String category,
            Pageable pageable
    ) {
        PageResponse<ProductDocumentInfo> result = productSearchService.searchProductDocument(keyword, sellerId, category, pageable);
        return new ResponseDto<>(HttpStatus.OK, result, "상품 문서 검색 완료.");
    }
}
