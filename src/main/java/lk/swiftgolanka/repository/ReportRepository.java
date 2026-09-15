package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.Report;
import lk.swiftgolanka.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByGeneratedByIdOrderByCreatedAtDesc(Long generatedById);

    List<Report> findByReportTypeOrderByCreatedAtDesc(ReportType reportType);

    List<Report> findAllByOrderByCreatedAtDesc();
}
