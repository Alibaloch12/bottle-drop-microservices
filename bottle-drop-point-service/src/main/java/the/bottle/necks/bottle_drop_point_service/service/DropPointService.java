package the.bottle.necks.bottle_drop_point_service.service;

import org.springframework.stereotype.Service;
import the.bottle.necks.bottle_drop_point_service.entity.DropPoint;
import the.bottle.necks.bottle_drop_point_service.repository.DropPointRepository;

import java.util.List;

@Service
public class DropPointService {

    private final DropPointRepository dropPointRepository;
    private final WarehouseClient warehouseClient;

    public DropPointService(DropPointRepository dropPointRepository,
                            WarehouseClient warehouseClient) {
        this.dropPointRepository = dropPointRepository;
        this.warehouseClient = warehouseClient;
    }

    public List<DropPoint> findAll() {
        return dropPointRepository.findAll();
    }

    public DropPoint findById(Long id) {
        return dropPointRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Drop point not found."));
    }

    public void submitReturn(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        DropPoint dropPoint = findById(id);
        dropPoint.acceptReturn(quantity);
        dropPointRepository.save(dropPoint);
    }

    public void emptyDropPoint(Long id) {
        DropPoint dropPoint = findById(id);

        if (dropPoint.getCurrentFillLevel() == 0) {
            throw new IllegalArgumentException("Drop point is already empty.");
        }

        int quantityToTransfer = dropPoint.getCurrentFillLevel();

        try {
            warehouseClient.transferEmpties(id, quantityToTransfer);
        } catch (Exception e) {
            System.out.println("Warehouse service unavailable. Skipping transfer for now.");
        }

        dropPoint.emptyDropPoint();
        dropPointRepository.save(dropPoint);
    }
}