package com.rideaustin.repo.jpa;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.user.User;

/**
 * defines the database access methods of the user entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, QueryDslPredicateExecutor<User> {

  @Query("SELECT u FROM User u WHERE u.id=?1")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  User findOneForUpdate(long id);
}
