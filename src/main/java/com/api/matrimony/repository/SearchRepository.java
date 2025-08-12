package com.api.matrimony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.UserProfile;

@Repository
public interface SearchRepository extends JpaRepository<UserProfile, Long>,JpaSpecificationExecutor<UserProfile>  {

	
}
