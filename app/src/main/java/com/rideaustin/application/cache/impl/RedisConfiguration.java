package com.rideaustin.application.cache.impl;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.CustomConversions;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.rideaustin.application.cache.impl.RedisConverters.AreaToBytesConverter;
import com.rideaustin.application.cache.impl.RedisConverters.BytesToAreaConverter;
import com.rideaustin.application.cache.impl.RedisConverters.BytesToSessionConverter;
import com.rideaustin.application.cache.impl.RedisConverters.SessionToBytesConverter;
import com.rideaustin.dispatch.InceptionMachinesSubscriber;
import com.rideaustin.model.Area;
import com.rideaustin.model.Session;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.service.user.BlockedDeviceRegistry;

import lombok.Getter;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Getter
@EnableRedisRepositories(basePackages = "com.rideaustin.repo.redis", keyspaceConfiguration = RedisConfiguration.PrefixedKeyspaceConfiguration.class)
public class RedisConfiguration {

  private static final Long REDIS_BORROW_TIMEOUT = 2500L;
  private static final String CHANNEL_DRIVER_SESSION_CLOSED = "%s:channel_driver_session_closed";
  private static final String CHANNEL_INCEPTION_MACHINES = "%s:channel_inception_machines";
  private static final String CHANNEL_BLOCKED_DEVICES = "%s:channel_blocked_devices";

  @Bean(name = "jedisSubscribeExecutor")
  public TaskExecutor subscribeExecutor() {
    return new SimpleAsyncTaskExecutor("JedisSubscribeThread-");
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory(Environment environment) {
    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
    jedisConnectionFactory.setHostName(environment.getProperty("cache.redis.host"));
    jedisConnectionFactory.setPort(environment.getProperty("cache.redis.port", Integer.class));
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxWaitMillis(REDIS_BORROW_TIMEOUT);
    poolConfig.setMaxTotal(environment.getProperty("cache.redis.max.active", Integer.class));
    poolConfig.setMaxIdle(environment.getProperty("cache.redis.max.idle", Integer.class));
    poolConfig.setMaxWaitMillis(10000);
    jedisConnectionFactory.setPoolConfig(poolConfig);
    return jedisConnectionFactory;
  }

  @Bean
  @Primary
  public RedisTemplate<byte[], byte[]> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
  }

  @Bean
  public RedisKeyValueAdapter redisKeyValueAdapter(RedisTemplate template, CustomConversions customConversions, Environment environment) {
    return new RedisKeyValueAdapter(template, new RedisMappingContext(new MappingConfiguration(new IndexConfiguration(), new PrefixedKeyspaceConfiguration(environment))), customConversions);
  }

  @Bean
  public CustomConversions redisCustomConversions(
    SessionToBytesConverter sessionToBytesConverter, BytesToSessionConverter bytesToSessionConverter,
    AreaToBytesConverter areaToBytesConverter, BytesToAreaConverter bytesToAreaConverter) {
    return new CustomConversions(ImmutableList.of(sessionToBytesConverter, bytesToSessionConverter,
      areaToBytesConverter, bytesToAreaConverter));
  }

  @Bean
  public Jackson2HashMapper jackson2HashMapper(ObjectMapper mapper) {
    return new Jackson2HashMapper(mapper, true);
  }

  @Bean
  public MessageListenerAdapter sessionClosedListener(SessionClosedSubscriber sessionClosedSubscriber) {
    MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(sessionClosedSubscriber);
    listenerAdapter.setSerializer(new JdkSerializationRedisSerializer());
    return listenerAdapter;
  }

  @Bean
  public MessageListenerAdapter inceptionMachinesListener(InceptionMachinesSubscriber inceptionMachinesSubscriber) {
    MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(inceptionMachinesSubscriber);
    listenerAdapter.setSerializer(new JdkSerializationRedisSerializer());
    return listenerAdapter;
  }

  @Bean
  public MessageListenerAdapter blockedDevicesListener(BlockedDeviceRegistry blockedDeviceRegistry) {
    MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(blockedDeviceRegistry);
    listenerAdapter.setSerializer(new JdkSerializationRedisSerializer());
    return listenerAdapter;
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(JedisConnectionFactory connectionFactory,
    Environment environment, @Qualifier("sessionClosedListener") MessageListenerAdapter sessionClosedListener,
    @Qualifier("inceptionMachinesListener") MessageListenerAdapter inceptionMachinesListener,
    @Qualifier("blockedDevicesListener") MessageListenerAdapter blockedDevicesListener) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setTaskExecutor(subscribeExecutor());
    container.addMessageListener(sessionClosedListener, sessionClosedTopic(environment));
    container.addMessageListener(inceptionMachinesListener, inceptionMachinesTopic(environment));
    container.addMessageListener(blockedDevicesListener, blockedDevicesTopic(environment));
    return container;
  }

  @Bean
  public ChannelTopic sessionClosedTopic(Environment environment) {
    return new ChannelTopic(String.format(CHANNEL_DRIVER_SESSION_CLOSED, getKeyPrefix(environment)));
  }

  @Bean
  public ChannelTopic inceptionMachinesTopic(Environment environment) {
    return new ChannelTopic(String.format(CHANNEL_INCEPTION_MACHINES, getKeyPrefix(environment)));
  }

  @Bean
  public ChannelTopic blockedDevicesTopic(Environment environment) {
    return new ChannelTopic(String.format(CHANNEL_BLOCKED_DEVICES, getKeyPrefix(environment)));
  }

  @Component
  public static class PrefixedKeyspaceConfiguration extends KeyspaceConfiguration {

    private final String keyPrefix;

    @Inject
    public PrefixedKeyspaceConfiguration(Environment environment) {
      keyPrefix = getKeyPrefix(environment);
      addKeyspaceSettings(createKeyspaceSettings(RedisSurgeArea.class));
      addKeyspaceSettings(createKeyspaceSettings(Session.class));
      addKeyspaceSettings(createKeyspaceSettings(Area.class));
    }

    private KeyspaceSettings createKeyspaceSettings(Class<?> type) {
      return new KeyspaceSettings(type, String.format("%s-%s", keyPrefix, type.getSimpleName()));
    }
  }

  private static String getKeyPrefix(Environment environment) {
    return environment.getProperty("cache.redis.key.prefix", String.class, "");
  }

}
