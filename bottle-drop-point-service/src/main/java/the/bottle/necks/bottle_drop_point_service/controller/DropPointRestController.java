package the.bottle.necks.bottle_drop_point_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import the.bottle.necks.bottle_drop_point_service.entity.DropPoint;
import the.bottle.necks.bottle_drop_point_service.entity.ReportRequest;
import the.bottle.necks.bottle_drop_point_service.service.DropPointService;
import the.bottle.necks.bottle_drop_point_service.service.ReportRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/drop-points")
public class DropPointRestController {

    private final DropPointService dropPointService;
    private final ReportRequestService reportRequestService;

    public DropPointRestController(DropPointService dropPointService, ReportRequestService reportRequestService) {
        this.dropPointService = dropPointService;
        this.reportRequestService = reportRequestService;
    }

    @GetMapping
    public ResponseEntity<List<DropPoint>> listDropPoints() {
        return ResponseEntity.ok(dropPointService.findAll());
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<String> submitReturn(@PathVariable Long id, @RequestParam int quantity) {
        try {
            dropPointService.submitReturn(id, quantity);
            return ResponseEntity.ok("Return processed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<String> submitReport(@PathVariable Long id) {
        try {
            DropPoint dropPoint = dropPointService.findById(id);
            if (dropPoint == null) {
                return ResponseEntity.notFound().build();
            }
            reportRequestService.reportFull(id);
            return ResponseEntity.ok("Report submitted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/empty")
    public ResponseEntity<String> emptyDropPoint(@PathVariable Long id) {
        try {
            dropPointService.emptyDropPoint(id);
            return ResponseEntity.ok("Drop point emptied successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportRequest>> listReports() {
        return ResponseEntity.ok(reportRequestService.findAll());
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<String> resolveReport(@PathVariable Long id) {
        try {
            reportRequestService.resolveReport(id);
            return ResponseEntity.ok("Report resolved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}