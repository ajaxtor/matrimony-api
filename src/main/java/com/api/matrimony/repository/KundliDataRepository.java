package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.KundliData;
import com.api.matrimony.enums.ManglikStatus;

/**
 * Kundli Data Repository
 */
@Repository
public interface KundliDataRepository extends JpaRepository<KundliData, Long> {
    
    Optional<KundliData> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    List<KundliData> findByManglikStatus(ManglikStatus manglikStatus);
    
    @Query("SELECT k FROM KundliData k WHERE k.rashi = :rashi")
    List<KundliData> findByRashi(@Param("rashi") String rashi);
}

