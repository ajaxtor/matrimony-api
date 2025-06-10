package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.Conversation;

/**
 * Conversation Repository
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1.id = :userId OR c.user2.id = :userId) AND c.isActive = true " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Conversation c WHERE " +
           "((c.user1.id = :user1Id AND c.user2.id = :user2Id) OR " +
           "(c.user1.id = :user2Id AND c.user2.id = :user1Id)) AND c.isActive = true")
    Optional<Conversation> findByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    Optional<Conversation> findByMatchId(Long matchId);
}
