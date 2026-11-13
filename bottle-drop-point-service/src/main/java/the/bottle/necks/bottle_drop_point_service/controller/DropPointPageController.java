package the.bottle.necks.bottle_drop_point_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import the.bottle.necks.bottle_drop_point_service.service.DropPointService;
import the.bottle.necks.bottle_drop_point_service.service.ReportRequestService;

@Controller
public class DropPointPageController {

    private final DropPointService dropPointService;
    private final ReportRequestService reportRequestService;

    public DropPointPageController(DropPointService dropPointService, ReportRequestService reportRequestService) {
        this.dropPointService = dropPointService;
        this.reportRequestService = reportRequestService;
    }

    @GetMapping("/drop-points")
    public String showDropPoints(Model model) {
        model.addAttribute("dropPoints", dropPointService.findAll());
        return "drop-points/list";
    }

    @GetMapping("/drop-points/reports")
    public String showReports(Model model) {
        model.addAttribute("reports", reportRequestService.findAll());
        return "drop-points/reports";
    }

    @PostMapping("/drop-points/{id}/return")
    public String submitReturn(@PathVariable Long id, @RequestParam int quantity, Model model) {
        try {
            dropPointService.submitReturn(id, quantity);
            return "redirect:/drop-points";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("dropPoints", dropPointService.findAll());
            return "drop-points/list";
        }
    }

    @PostMapping("/drop-points/{id}/report")
    public String submitReport(@PathVariable Long id, Model model) {
        try {
            reportRequestService.reportFull(id);
            return "redirect:/drop-points/reports";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("dropPoints", dropPointService.findAll());
            return "drop-points/list";
        }
    }

    @PostMapping("/drop-points/{id}/empty")
    public String emptyDropPoint(@PathVariable Long id, Model model) {
        try {
            dropPointService.emptyDropPoint(id);
            return "redirect:/drop-points";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("dropPoints", dropPointService.findAll());
            return "drop-points/list";
        }
    }

    @PostMapping("/drop-points/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, Model model) {
        try {
            reportRequestService.resolveReport(id);
            return "redirect:/drop-points/reports";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("reports", reportRequestService.findAll());
            return "drop-points/reports";
        }
    }
}
