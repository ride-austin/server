package com.rideaustin.repo.redis;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.redis.RedisSurgeArea;

@Repository
public interface SurgeAreaRedisRepository extends CrudRepository<RedisSurgeArea, Long> {

  List<RedisSurgeArea> findByCityId(Long cityId);
}
