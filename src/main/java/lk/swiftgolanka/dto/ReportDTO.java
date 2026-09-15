package lk.swiftgolanka.dto;

import jakarta.validation.constraints.NotNull;
import lk.swiftgolanka.enums.ReportType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReportRequestDTO {

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    private String title;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    public ReportRequestDTO() {}

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
