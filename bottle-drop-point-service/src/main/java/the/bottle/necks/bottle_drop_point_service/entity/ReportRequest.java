package the.bottle.necks.bottle_drop_point_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ReportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime reportTime;

    private boolean resolved;

    protected ReportRequest() {
    }

    public ReportRequest(LocalDateTime reportTime) {
        if (reportTime == null) {
            throw new IllegalArgumentException("Report time is required.");
        }

        this.reportTime = reportTime;
        this.resolved = false;
    }

    public void resolve() {
        if (this.resolved) {
            throw new IllegalArgumentException("Report is already resolved.");
        }

        this.resolved = true;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReportTime(LocalDateTime reportTime) {
        if (reportTime == null) {
            throw new IllegalArgumentException("Report time is required.");
        }

        this.reportTime = reportTime;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}