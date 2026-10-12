package lk.swiftgolanka.dto;

import lk.swiftgolanka.entity.Report;
import java.util.List;
import java.util.Map;

public class GeneratedReportDTO {
    private Report report;
    private List<String> headers;
    private List<List<String>> rows;
    private Map<String, String> summaryStats;

    public GeneratedReportDTO() {}

    public GeneratedReportDTO(Report report, List<String> headers, List<List<String>> rows, Map<String, String> summaryStats) {
        this.report = report;
        this.headers = headers;
        this.rows = rows;
        this.summaryStats = summaryStats;
    }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public List<List<String>> getRows() { return rows; }
    public void setRows(List<List<String>> rows) { this.rows = rows; }
    public Map<String, String> getSummaryStats() { return summaryStats; }
    public void setSummaryStats(Map<String, String> summaryStats) { this.summaryStats = summaryStats; }
}
