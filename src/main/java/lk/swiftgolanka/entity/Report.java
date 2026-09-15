package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by_user_id", nullable = false)
    private User generatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Report() {}

    public Report(Long id, ReportType reportType, String title, LocalDate startDate, LocalDate endDate, User generatedBy, LocalDateTime createdAt) {
        this.id = id;
        this.reportType = reportType;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.generatedBy = generatedBy;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public static class ReportBuilder {
        private Long id;
        private ReportType reportType;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private User generatedBy;
        private LocalDateTime createdAt;

        public ReportBuilder id(Long id) { this.id = id; return this; }
        public ReportBuilder reportType(ReportType reportType) { this.reportType = reportType; return this; }
        public ReportBuilder title(String title) { this.title = title; return this; }
        public ReportBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public ReportBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public ReportBuilder generatedBy(User generatedBy) { this.generatedBy = generatedBy; return this; }
        public ReportBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Report build() {
            return new Report(id, reportType, title, startDate, endDate, generatedBy, createdAt);
        }
    }
}
