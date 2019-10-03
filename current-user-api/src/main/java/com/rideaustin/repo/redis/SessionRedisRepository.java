package com.rideaustin.repo.redis;

import org.springframework.data.repository.CrudRepository;

import com.rideaustin.model.Session;

public interface SessionRedisRepository extends CrudRepository<Session, String> {
}
