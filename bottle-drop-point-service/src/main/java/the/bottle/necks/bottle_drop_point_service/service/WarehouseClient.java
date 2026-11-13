package the.bottle.necks.bottle_drop_point_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import the.bottle.necks.bottle_drop_point_service.web.EmptiesTransferRequest;

@Service
public class WarehouseClient {

    private final RestTemplate restTemplate;

    public WarehouseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void transferEmpties(Long dropPointId, int quantity) {
        EmptiesTransferRequest request = new EmptiesTransferRequest(dropPointId, quantity);

        restTemplate.postForObject(
                "http://localhost:8082/api/warehouse/empties-transfer",
                request,
                Void.class
        );
    }
}