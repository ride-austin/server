package com.rideaustin.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.User;

@Repository
public interface ActiveDriverRepository extends JpaRepository<ActiveDriver, Long>,
  QueryDslPredicateExecutor<ActiveDriver> {

  @Query("select ad from ActiveDriver ad where ad.driver.user = ?1 and ad.status <> 'INACTIVE'")
  ActiveDriver findByUserAndNotInactiveStatus(User user);

}
