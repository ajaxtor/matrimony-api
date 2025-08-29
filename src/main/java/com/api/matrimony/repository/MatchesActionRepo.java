package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.MatchesAction;

@Repository
public interface MatchesActionRepo extends JpaRepository<MatchesAction, Long> {

    Optional<MatchesAction> findByMatchId(String matchId);
    List<MatchesAction> findByFromUserId(Long fromUserId);
    
    @Query(value = "SELECT * FROM matches_action WHERE from_user_id = :fromUserId AND status = 'PENDING' ", nativeQuery = true)
    List<MatchesAction> findSendingRequestsByFromUser(@Param("fromUserId") Long fromUserId);

    @Query(value = "SELECT * FROM matches_action WHERE to_user_id = :toUserId AND status = 'PENDING'", nativeQuery = true)
    List<MatchesAction> findPendingRequestsByToUser(@Param("toUserId") Long toUserId);
    
    @Query(value = "SELECT * FROM matches_action WHERE match_id = :matchId AND status IN ('PENDING','MUTUAL')", nativeQuery = true)
    List<MatchesAction> findByMatchIdAndStatus(@Param("matchId") String matchId);

    @Query(value = "SELECT * FROM matrimony_app.matches_action WHERE from_user_id = :fromUserId AND status = 'MUTUAL'", 
    	       nativeQuery = true)
    	List<MatchesAction> findMutualMatchesByFromUserId(@Param("fromUserId") Long fromUserId);
    
    @Query(value = "SELECT * FROM matrimony_app.matches_action WHERE from_user_id = :fromUserId AND status = 'REJECTED'", 
 	       nativeQuery = true)
 	List<MatchesAction> findRejectedListByFromUserId(@Param("fromUserId") Long fromUserId);

}

