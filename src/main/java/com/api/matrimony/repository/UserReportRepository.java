package com.api.matrimony.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.UserReport;
import com.api.matrimony.enums.ReportStatus;

/**
 * User Report Repository
 */
@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    
    List<UserReport> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId);
    
    List<UserReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    
    Page<UserReport> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.reportedUser.id = :userId")
    Long countReportsByUserId(@Param("userId") Long userId);
    
    boolean existsByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);
}
