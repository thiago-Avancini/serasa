package br.com.serasa.serasa.report;

import br.com.serasa.serasa.report.dto.BranchReportItem;
import br.com.serasa.serasa.report.dto.GrainTypeReportItem;
import br.com.serasa.serasa.report.dto.OverviewReport;
import br.com.serasa.serasa.report.dto.ScaleReportItem;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/grain-types")
    public List<GrainTypeReportItem> grainTypes() {
        return service.grainTypeReport();
    }

    @GetMapping("/branches")
    public List<BranchReportItem> branches() {
        return service.branchReport();
    }

    @GetMapping("/scales")
    public List<ScaleReportItem> scales() {
        return service.scaleReport();
    }

    @GetMapping("/overview")
    public OverviewReport overview() {
        return service.overview();
    }
}