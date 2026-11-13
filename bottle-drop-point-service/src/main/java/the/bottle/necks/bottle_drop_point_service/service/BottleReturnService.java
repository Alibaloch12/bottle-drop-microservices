package the.bottle.necks.bottle_drop_point_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import the.bottle.necks.bottle_drop_point_service.entity.DropPoint;
import the.bottle.necks.bottle_drop_point_service.entity.ReportRequest;
import the.bottle.necks.bottle_drop_point_service.repository.DropPointRepository;
import the.bottle.necks.bottle_drop_point_service.repository.ReportRequestRepository;

import java.time.LocalDateTime;

@Service
public class BottleReturnService {

    private final DropPointRepository dropPointRepository;
    private final ReportRequestRepository reportRequestRepository;

    public BottleReturnService(DropPointRepository dropPointRepository,
                               ReportRequestRepository reportRequestRepository) {
        this.dropPointRepository = dropPointRepository;
        this.reportRequestRepository = reportRequestRepository;
    }

    @Transactional
    public void submitReturn(Long dropPointId, int quantity) {
        DropPoint dropPoint = dropPointRepository.findById(dropPointId)
                .orElseThrow(() -> new IllegalArgumentException("Drop point not found."));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        int currentFillLevel = dropPoint.getCurrentFillLevel();
        int maxCapacity = dropPoint.getMaxCapacity();
        int remainingCapacity = maxCapacity - currentFillLevel;

        if (remainingCapacity <= 0) {
            throw new IllegalStateException("This drop point is already full.");
        }

        if (quantity > remainingCapacity) {
            throw new IllegalStateException(
                    "Return exceeds remaining capacity. Only " + remainingCapacity + " bottles can be accepted."
            );
        }

        dropPoint.setCurrentFillLevel(currentFillLevel + quantity);
        dropPointRepository.save(dropPoint);
    }

    @Transactional
    public void reportFullDropPoint(Long dropPointId) {
        DropPoint dropPoint = dropPointRepository.findById(dropPointId)
                .orElseThrow(() -> new IllegalArgumentException("Drop point not found."));

        if (dropPoint.getCurrentFillLevel() < dropPoint.getMaxCapacity()) {
            throw new IllegalStateException("This drop point is not full yet.");
        }

        ReportRequest reportRequest = new ReportRequest(LocalDateTime.now());
        reportRequestRepository.save(reportRequest);
    }

    @Transactional
    public void resolveReport(Long reportId) {
        ReportRequest report = reportRequestRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));

        report.resolve();
        reportRequestRepository.save(report);
    }

    @Transactional
    public void emptyDropPoint(Long dropPointId) {
        DropPoint dropPoint = dropPointRepository.findById(dropPointId)
                .orElseThrow(() -> new IllegalArgumentException("Drop point not found."));

        if (dropPoint.getCurrentFillLevel() == 0) {
            throw new IllegalStateException("This drop point is already empty.");
        }

        dropPoint.setCurrentFillLevel(0);
        dropPointRepository.save(dropPoint);
    }
}