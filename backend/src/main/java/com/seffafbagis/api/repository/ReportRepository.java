package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.report.Report;
import com.seffafbagis.api.enums.ReportEntityType;
import com.seffafbagis.api.enums.ReportPriority;
import com.seffafbagis.api.enums.ReportStatus;
import com.seffafbagis.api.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

    Page<Report> findByStatusIn(Collection<ReportStatus> statuses, Pageable pageable);

    Page<Report> findByPriority(ReportPriority priority, Pageable pageable);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    boolean existsByReporterIdAndEntityTypeAndEntityIdAndStatusNot(UUID reporterId, ReportEntityType entityType,
            UUID entityId, ReportStatus status);

    Page<Report> findByReporterId(UUID reporterId, Pageable pageable);

    Page<Report> findByStatusOrderByPriorityDescCreatedAtAsc(ReportStatus status, Pageable pageable);

    Page<Report> findByReportTypeAndStatus(ReportType reportType, ReportStatus status, Pageable pageable);

    Page<Report> findByEntityTypeAndEntityId(ReportEntityType entityType, UUID entityId, Pageable pageable);

    Page<Report> findByAssignedToId(UUID assignedToId, Pageable pageable);

    long countByStatus(ReportStatus status);
}
