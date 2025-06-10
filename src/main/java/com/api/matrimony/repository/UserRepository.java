package com.api.matrimony.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.User;
import com.api.matrimony.enums.UserType;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
    List<User> findByIsActiveTrue();
    List<User> findByIsVerifiedFalse();
    
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.isActive = true AND u.isVerified = true")
    Page<User> findActiveUsersByType(@Param("userType") UserType userType, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    Long countNewUsersFromDate(@Param("date") LocalDateTime date);
}