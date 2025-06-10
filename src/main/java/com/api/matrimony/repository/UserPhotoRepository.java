package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.UserPhoto;

/**
 * User Photo Repository
 */
@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {
    
    List<UserPhoto> findByUserIdOrderByDisplayOrderAsc(Long userId);
    
    Optional<UserPhoto> findByUserIdAndIsPrimaryTrue(Long userId);
    
    void deleteByUserId(Long userId);
    
    @Query("SELECT COUNT(p) FROM UserPhoto p WHERE p.user.id = :userId")
    Long countPhotosByUserId(@Param("userId") Long userId);
}
