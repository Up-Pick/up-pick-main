package org.oneog.uppick.domain.searching.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.common.dto.GlobalPageResponse;
import org.oneog.uppick.domain.searching.dto.request.SearchProductRequest;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.domain.searching.service.SearchingInternalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/searching")
public class SearchingController {
    private final SearchingInternalService searchingInternalService;

    @PostMapping("/products")
    public GlobalApiResponse<GlobalPageResponse<SearchProductInfoResponse>> searchProduct(
        @Valid @RequestBody(required = false)
        SearchProductRequest searchProductRequest) {
        if (searchProductRequest == null) {
            searchProductRequest = SearchProductRequest.ofDefault();
        }
        return GlobalApiResponse.ok(
            GlobalPageResponse.of(searchingInternalService.searchProduct(searchProductRequest)));
    }
}
