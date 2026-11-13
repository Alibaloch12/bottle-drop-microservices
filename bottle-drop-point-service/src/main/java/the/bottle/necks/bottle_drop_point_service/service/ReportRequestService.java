package the.bottle.necks.bottle_drop_point_service.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import the.bottle.necks.bottle_drop_point_service.entity.DropPoint;
import the.bottle.necks.bottle_drop_point_service.entity.ReportRequest;
import the.bottle.necks.bottle_drop_point_service.repository.DropPointRepository;
import the.bottle.necks.bottle_drop_point_service.repository.ReportRequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportRequestService {

    private final ReportRequestRepository reportRequestRepository;
    private final DropPointRepository dropPointRepository;

    public ReportRequestService(ReportRequestRepository reportRequestRepository,
                                DropPointRepository dropPointRepository) {
        this.reportRequestRepository = reportRequestRepository;
        this.dropPointRepository = dropPointRepository;
    }

    public void reportFull(Long dropPointId) {
        DropPoint dropPoint = dropPointRepository.findById(dropPointId)
                .orElseThrow(() -> new IllegalArgumentException("Drop point not found."));

        if (!dropPoint.isFull()) {
            throw new IllegalArgumentException("This drop point is not full yet.");
        }

        ReportRequest reportRequest = new ReportRequest(LocalDateTime.now());
        reportRequestRepository.save(reportRequest);
    }

    public List<ReportRequest> findAll() {
        return reportRequestRepository.findAll(
                Sort.by(Sort.Direction.DESC, "reportTime")
        );
    }

    public void resolveReport(Long id) {
        ReportRequest reportRequest = reportRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report request not found."));

        if (reportRequest.isResolved()) {
            throw new IllegalArgumentException("Report is already resolved.");
        }

        reportRequest.resolve();
        reportRequestRepository.save(reportRequest);
    }
}