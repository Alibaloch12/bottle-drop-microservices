package com.purchase_service.Bottle.Drop.System.service;


import com.purchase_service.Bottle.Drop.System.dto.request.RecordBarSaleRequest;
import com.purchase_service.Bottle.Drop.System.dto.response.BarSaleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BarServiceClient {

    private final RestClient restClient;

    @Value("${bar-service.base-url}")
    private String barServiceBaseUrl;

    @Value("${bar-service.internal-api-key}")
    private String internalApiKey;

    public BarSaleResponse recordSale(
            UUID barId,
            RecordBarSaleRequest request
    ) {
        return restClient.post()
                .uri(barServiceBaseUrl + "/api/v1/bars/{barId}/sales", barId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Api-Key", internalApiKey)
                .body(request)
                .retrieve()
                .body(BarSaleResponse.class);
    }
}