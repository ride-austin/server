package com.rideaustin.application.cache.impl;

import javax.inject.Inject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.Area;
import com.rideaustin.model.Session;

public final class RedisConverters {

  private RedisConverters(){}

  abstract static class AbstractWritingConverter<S> implements Converter<S, byte[]> {
    private final Jackson2JsonRedisSerializer<S> serializer;

    AbstractWritingConverter(ObjectMapper objectMapper, Class<S> clazz) {
      serializer = new Jackson2JsonRedisSerializer<>(clazz);
      serializer.setObjectMapper(objectMapper);
    }

    @Override
    public byte[] convert(S s) {
      return serializer.serialize(s);
    }
  }

  abstract static class AbstractReadingConverter<S> implements Converter<byte[], S> {
    private final Jackson2JsonRedisSerializer<S> serializer;

    AbstractReadingConverter(ObjectMapper objectMapper, Class<S> clazz) {
      serializer = new Jackson2JsonRedisSerializer<>(clazz);
      serializer.setObjectMapper(objectMapper);
    }

    @Override
    public S convert(byte[] bytes) {
      return serializer.deserialize(bytes);
    }
  }

  /**
   * com.rideaustin.model.Session converters
   */
  @WritingConverter
  @Component
  public static class SessionToBytesConverter extends AbstractWritingConverter<Session> {
    @Inject
    SessionToBytesConverter(ObjectMapper objectMapper) {
      super(objectMapper, Session.class);
    }
  }

  @ReadingConverter
  @Component
  public static class BytesToSessionConverter extends AbstractReadingConverter<Session> {
    @Inject
    BytesToSessionConverter(ObjectMapper mapper) {
      super(mapper, Session.class);
    }
  }

  /**
   * com.rideaustin.model.Area converters
   */
  @WritingConverter
  @Component
  public static class AreaToBytesConverter extends AbstractWritingConverter<Area> {
    @Inject
    AreaToBytesConverter(ObjectMapper objectMapper) {
      super(objectMapper, Area.class);
    }
  }

  @ReadingConverter
  @Component
  public static class BytesToAreaConverter extends AbstractReadingConverter<Area> {
    @Inject
    BytesToAreaConverter(ObjectMapper mapper) {
      super(mapper, Area.class);
    }
  }

}
