package com.rideaustin.application.cache.impl;

import java.util.Collection;
import java.util.function.Function;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
@Component
public class JedisClient {

  private JedisPool jedisPool;

  public JedisClient(JedisConnectionFactory jedisConfiguration) {
    jedisPool = new JedisPool(jedisConfiguration.getPoolConfig(), jedisConfiguration.getHostName(), jedisConfiguration.getPort(), 10000);
  }

  public void put(String key, String value) {
    executeJedisOp(jedis -> jedis.set(key, value));
  }

  public void remove(String key) {
    executeJedisOp(jedis -> jedis.del(key));
  }

  public void remove(String key, String member) {
    executeJedisOp(jedis -> jedis.srem(key, member));
  }

  public void remove(String key, Collection<String> members) {
    executeJedisOp(jedis -> jedis.srem(key, members.toArray(new String[0])));
  }

  public void flushAll() {
    executeJedisOp(BinaryJedis::flushAll);
  }

  public void addToSet(String key, String id) {
    executeJedisOp(jedis -> jedis.sadd(key, id));
  }

  public boolean isMemberOf(String key, String id) {
    return executeJedisOp(jedis -> jedis.sismember(key, id));
  }

  public boolean exists(String key) {
    return executeJedisOp(jedis -> jedis.exists(key));
  }

  private <T> T executeJedisOp(Function<Jedis, T> function) {
    try (Jedis jedis = jedisPool.getResource()) {
      return function.apply(jedis);
    }
  }

}
