package com.rideaustin.service;

import org.springframework.core.env.Environment;

import com.rideaustin.application.cache.impl.JedisClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestedDriversRegistry {

  private static final String KEY_TEMPLATE = "%s:REQUESTED";

  private final JedisClient jedisClient;
  private final String prefix;

  public RequestedDriversRegistry(JedisClient jedisClient, Environment environment) {
    this.jedisClient = jedisClient;
    this.prefix = environment.getProperty("cache.redis.key.prefix", String.class, "");
  }

  public void addRequested(long id) {
    jedisClient.addToSet(String.format(KEY_TEMPLATE, prefix), String.valueOf(id));
  }

  public boolean isRequested(long id) {
    return jedisClient.isMemberOf(String.format(KEY_TEMPLATE, prefix), String.valueOf(id));
  }

  public void remove(long id) {
    jedisClient.remove(String.format(KEY_TEMPLATE, prefix), String.valueOf(id));
  }
}
