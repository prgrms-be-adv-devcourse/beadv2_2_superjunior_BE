package store._0982.elasticsearch.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import store._0982.elasticsearch.presentation.dto.ProductDocumentRequset;

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

    @Operation(summary = "상품 문서 추가", description = "상품 Elasticsearch 인덱스에 검색용 문서를 추가한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping
    public ResponseDto<ProductDocumentInfo> saveProductDocument(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "상품 색인 요청",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "productId": "c3b42ab9-8d20-4d5c-a0b2-3a91bcbcd111",
                                              "name": "아이폰 15 프로",
                                              "price": 1490000,
                                              "category": "ELECTRONICS",
                                              "description": "애플의 최신 스마트폰 아이폰 15 프로 모델입니다.",
                                              "stock": 120,
                                              "originalUrl": "https://example.com/images/iphone15pro.jpg",
                                              "sellerId": "c3b42ab9-8d20-4d5c-a0b2-3a91bcbcd123",
                                              "createdAt": "2025-03-08T09:00:00Z",
                                              "updatedAt": "2025-03-08T09:00:00Z"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody ProductDocumentRequset request
    ) {
        ProductDocumentInfo saved = productSearchService.saveProductDocument(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, saved, "상품 문서 생성 완료");
    }

    @Operation(summary = "상품 문서 검색", description = "키워드(제목, 설명) + sellerId 기준으로 상품을 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search")
    public ResponseDto<PageResponse<ProductDocumentInfo>> searchProductDocument(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam UUID sellerId,
            Pageable pageable
    ) {
        PageResponse<ProductDocumentInfo> result = productSearchService.searchProductDocument(keyword, sellerId, pageable);
        return new ResponseDto<>(HttpStatus.OK, result, "상품 문서 검색 완료.");
    }

    @Operation(summary = "상품 문서 삭제", description = "id 기준으로 상품 문서를 삭제합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @DeleteMapping("{productId}")
    public ResponseDto<Void> deleteProductDocument(@PathVariable UUID productId) {
        productSearchService.deleteProductDocument(productId);
        return new ResponseDto<>(HttpStatus.OK, null, "상품 문서 삭제 완료.");
    }
}
