package com.rideaustin.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>,QueryDslPredicateExecutor<Event> {

}
