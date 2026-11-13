package the.bottle.necks.bottle_drop_point_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import the.bottle.necks.bottle_drop_point_service.entity.ReportRequest;

public interface ReportRequestRepository extends JpaRepository<ReportRequest, Long> {
}