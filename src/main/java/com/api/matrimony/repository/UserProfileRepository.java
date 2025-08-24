package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.UserProfile;

/**
 * User Profile Repository
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long>  {
    
    //Optional<UserProfile> findByUserId(Long userId);
    
    @Query(value = "SELECT * FROM user_profiles WHERE user_id = :userId AND is_hide = false LIMIT 1", 
    	       nativeQuery = true)
    	Optional<UserProfile> findByUserIdAndIsHideFalse(@Param("userId") Long userId);

    
    @Query("SELECT p FROM UserProfile p WHERE p.user.isActive = true AND p.user.isVerified = true AND p.isHide = false")
    Page<UserProfile> findActiveProfiles(Pageable pageable);
    
    @Query("SELECT p FROM UserProfile p WHERE " +
           "(:city IS NULL OR p.city = :city) AND " +
           "(:state IS NULL OR p.state = :state) AND " +
           "(:religion IS NULL OR p.religion = :religion) AND " +
           "(:minAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) >= :minAge) AND " +
           "(:maxAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= :maxAge) AND " +
           "p.user.isActive = true AND p.user.isVerified = true AND p.isHide = false")
    Page<UserProfile> findProfilesWithFilters(
            @Param("city") String city,
            @Param("state") String state,
            @Param("religion") String religion,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable);

    @Query(value = "SELECT * FROM user_profiles WHERE user_id <> :loginUserId AND is_hide = false", 
    	       nativeQuery = true)
    	List<UserProfile> findAllByUserIdNotAndIsHideFalse(@Param("loginUserId") Long loginUserId);

	
	@Query(value = "SELECT * FROM user_profiles " +
            "WHERE LOWER(full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND gender = :gender AND is_hide = false",
    nativeQuery = true)
List<UserProfile> searchByFullNameAndGender(@Param("keyword") String keyword,
                                         @Param("gender") String gender);


	Optional<UserProfile> findByUserId(Long id);



	
//	Page<UserProfile> findAll(Specification<UserProfile> spec, Pageable pageable);

	
}