package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.BlockedUser;

/**
 * Blocked User Repository
 */
@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    
    List<BlockedUser> findByBlockerIdOrderByBlockedAtDesc(Long blockerId);
    
    boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);
    
    Optional<BlockedUser> findByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);
    
    void deleteByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);
}

