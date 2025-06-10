package com.api.matrimony.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.SubscriptionPlan;

/**
* Subscription Plan Repository
*/
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
   
   List<SubscriptionPlan> findByIsActiveTrueOrderByPriceAsc();
   
   Optional<SubscriptionPlan> findByNameAndIsActiveTrue(String name);
}

