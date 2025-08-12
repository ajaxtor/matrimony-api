package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.Match;
import com.api.matrimony.enums.MatchStatus;

/**
 * Match Repository
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    List<Match> findByUserIdOrderByMatchedAtDesc(Long userId);
    
    List<Match> findByUserIdAndStatus(Long userId, MatchStatus status);
    
    Optional<Match> findByUserIdAndMatchedUserId(Long userId, Long matchedUserId);
    
    @Query("SELECT m FROM Match m WHERE m.user.id = :userId AND m.status = 'MUTUAL'")
    List<Match> findMutualMatchesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Match m WHERE " +
           "(m.user.id = :userId AND m.matchedUser.id = :matchedUserId) OR " +
           "(m.user.id = :matchedUserId AND m.matchedUser.id = :userId)")
    Optional<Match> findMatchBetweenUsers(@Param("userId") Long userId, @Param("matchedUserId") Long matchedUserId);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.user.id = :userId AND m.status = :status")
    Long countMatchesByUserIdAndStatus(@Param("userId") Long userId, @Param("status") MatchStatus status);
    
    Page<Match> findByUserIdAndStatusOrderByMatchedAtDesc(Long userId, MatchStatus status, Pageable pageable);
    
    List<Match> findByUserId(Long userId);
    
    List<Match> findByMatchedUserId(Long matchedUserId);
}