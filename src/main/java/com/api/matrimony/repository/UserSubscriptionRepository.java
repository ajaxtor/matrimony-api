package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.UserSubscription;
import com.api.matrimony.enums.SubscriptionStatus;

/**
 * User Subscription Repository
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    
    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT s FROM UserSubscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND s.endDate >= CURRENT_DATE")
    Optional<UserSubscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);
    
    @Query("SELECT s FROM UserSubscription s WHERE s.endDate < CURRENT_DATE AND s.status = 'ACTIVE'")
    List<UserSubscription> findExpiredSubscriptions();
    
    boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);

	    @Query(value = "SELECT * FROM matrimony_app.user_subscriptions WHERE order_id = :orderId ", nativeQuery = true)
	    Optional<UserSubscription> fetchByOrderIdAndReciptId(@Param("orderId") String orderId);

}

