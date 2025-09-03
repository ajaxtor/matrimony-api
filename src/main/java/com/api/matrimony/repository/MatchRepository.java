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
           "(m.user.id = :matchedUserId AND m.matchedUser.id = :userId) AND m.status = 'MUTUAL' ")
    Optional<Match> findMatchBetweenUsers(@Param("userId") Long userId, @Param("matchedUserId") Long matchedUserId);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.user.id = :userId AND m.status = :status")
    Long countMatchesByUserIdAndStatus(@Param("userId") Long userId, @Param("status") MatchStatus status);
    
    Page<Match> findByUserIdAndStatusOrderByMatchedAtDesc(Long userId, MatchStatus status, Pageable pageable);
    
    List<Match> findByUserId(Long userId);
    
    List<Match> findByMatchedUserId(Long matchedUserId);

	Optional<Match> findByMatchId(String matchId);

	@Query("SELECT m FROM Match m " +
		       "WHERE m.user.id = :userId " +
		       "AND m.matchedUser.id IN :matchedUserIds ")
		List<Match> findExistingMatches(@Param("userId") Long userId,
		                                @Param("matchedUserIds") List<Long> matchedUserIds);


	@Query(value = "SELECT * FROM matrimony_app.matches WHERE matched_user_id = :matchUserId AND user_id = :userId ", nativeQuery = true)
    Optional<Match> fetchByMatchedUserId(@Param("userId") Long userId,@Param("matchUserId") Long matchUserId);
	
	 @Query(value = "SELECT * FROM matrimony_app.matches WHERE match_id = :matchId AND user_id = :userId", nativeQuery = true)
	 Optional<Match> findByMatchIdAndUserId(@Param("matchId") String matchId, @Param("userId") Long userId);
	
}



