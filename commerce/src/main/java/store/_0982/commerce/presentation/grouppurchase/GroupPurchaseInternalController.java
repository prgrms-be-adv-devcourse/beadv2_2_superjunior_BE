package store._0982.commerce.presentation.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.commerce.application.grouppurchase.GroupPurchaseSearchService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.presentation.grouppurchase.dto.GroupPurchaseIdsRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/purchasees")
public class GroupPurchaseInternalController {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupPurchaseSearchRow> findByIds(@RequestBody GroupPurchaseIdsRequest request) {
        return groupPurchaseSearchService.findSearchRowsByIds(request.ids());
    }
}
