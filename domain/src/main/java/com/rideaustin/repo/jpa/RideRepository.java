package com.rideaustin.repo.jpa;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.rideaustin.model.ride.Ride;

public interface RideRepository extends JpaRepository<Ride, Long>,
  QueryDslPredicateExecutor<Ride> {

  @Query("SELECT r FROM Ride r WHERE r.id=?1")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Ride findOneForUpdate(long id);
}
