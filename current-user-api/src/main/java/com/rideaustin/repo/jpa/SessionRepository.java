package com.rideaustin.repo.jpa;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Session;
import com.rideaustin.model.user.User;

/**
 * defines the database access methods of the session entity
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, QueryDslPredicateExecutor<Session> {

  @Transactional(readOnly=true)
  @Query("select s from Session s where s.user.id = ?1 and s.deleted = false order by s.id desc")
  List<Session> findActiveSessionsByUser(Long userId);

  @Query("select new org.apache.commons.lang3.tuple.ImmutablePair(s.user.id, max(s.createdDate)) from Session s where s.user in (?1) group by s.user.id")
  List<Pair<Long, Date>> findLastLoginDateByUsers(List<User> user);

  @Query("select max(s.createdDate) as last_login from Session s where s.user = ?1")
  Date findLastLoginDateByUser(User user);

}
