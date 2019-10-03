package com.rideaustin.dispatch.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineContextRepository;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class RARedisStateMachineContextRepository implements StateMachineContextRepository<States, Events, StateMachineContext<States, Events>> {

  private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
    Kryo kryo = new Kryo();
    kryo.setDefaultSerializer(VersionFieldSerializer.class);
    kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
    kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
    kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
    return kryo;
  });

  private final RedisOperations<String,byte[]> redisOperations;

  /**
   * Instantiates a new redis state machine context repository.
   *
   * @param redisConnectionFactory the redis connection factory
   */
  public RARedisStateMachineContextRepository(RedisConnectionFactory redisConnectionFactory) {
    redisOperations = createDefaultTemplate(redisConnectionFactory);
  }

  @Override
  public void save(StateMachineContext<States, Events> context, String id) {
    redisOperations.opsForValue().set(id, serialize(context), 24, TimeUnit.HOURS);
  }

  @Override
  public StateMachineContext<States, Events> getContext(String id) {
    return deserialize(redisOperations.opsForValue().get(id));
  }

  private static RedisTemplate<String,byte[]> createDefaultTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String,byte[]> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setConnectionFactory(connectionFactory);
    template.afterPropertiesSet();
    return template;
  }

  private byte[] serialize(StateMachineContext<States, Events> context) {
    Kryo kryo = kryoThreadLocal.get();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Output output = new Output(out);
    kryo.writeObject(output, context);
    output.close();
    return out.toByteArray();
  }

  @SuppressWarnings("unchecked")
  private StateMachineContext<States, Events> deserialize(byte[] data) {
    if (data == null || data.length == 0) {
      return null;
    }
    Kryo kryo = kryoThreadLocal.get();
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    Input input = new Input(in);
    return kryo.readObject(input, StateMachineContext.class);
  }
}
