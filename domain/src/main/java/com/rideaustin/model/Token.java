package com.rideaustin.model;

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.rideaustin.filter.ClientAgentCity;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tokens")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token extends BaseEntity {

  public enum TokenEnvironment {
    DEV(0), PROD(1), UNDEFINED(-1);

    private int value;

    TokenEnvironment(int value) {
      this.value = value;
    }

    public int getValue() {
      return this.value;
    }

    public static TokenEnvironment parse(int id) {
      for (TokenEnvironment env : TokenEnvironment.values()) {
        if (env.getValue() == id) {
          return env;
        }
      }
      return TokenEnvironment.UNDEFINED;
    }
  }

  public enum TokenType {
    APPLE(0) {
      private final Set<String> apsProperties = Sets.newHashSet("alert", "sound");

      @Override
      public Map<String, Object> transform(Map<String, String> message, ObjectMapper mapper) {
        ImmutableMap.Builder<String, String> aps = ImmutableMap.builder();
        ImmutableMap.Builder<String, Object> result = ImmutableMap.builder();
        for (String prop : apsProperties) {
          if (message.containsKey(prop)) {
            aps.put(prop, message.get(prop));
            message.remove(prop);
          }
        }
        result.putAll(message);
        result.put("aps", aps.build());
        return result.build();
      }
    },

    GOOGLE(1) {

      static final String EVENT_KEY = "eventKey";

      @Override
      public Map<String, Object> transform(Map<String, String> message, ObjectMapper mapper) throws JsonProcessingException {
        ImmutableMap.Builder<String, Object> data = ImmutableMap.builder();
        data.put(EVENT_KEY, message.get(EVENT_KEY));
        message.remove(EVENT_KEY);
        data.put("body", mapper.writeValueAsString(message));
        return ImmutableMap.of("data", data.build());
      }
    }, UNDEFINED(-1) {
      @Override
      public Map<String, Object> transform(Map<String, String> message, ObjectMapper mapper) {
        return ImmutableMap.copyOf(message);
      }
    };

    private int value;

    TokenType(int value) {
      this.value = value;
    }

    public int getValue() {
      return this.value;
    }

    public static TokenType parse(int id) {
      for (TokenType type : TokenType.values()) {
        if (type.getValue() == id) {
          return type;
        }
      }
      return TokenType.UNDEFINED;
    }

    public abstract Map<String, Object> transform(Map<String, String> message, ObjectMapper mapper) throws JsonProcessingException;
  }

  @Column(nullable = false)
  private String value;

  @Column
  private int environment;

  @Column
  private int type;

  @Column(name = "agent_city")
  @Enumerated(EnumType.STRING)
  private ClientAgentCity agent;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "arn")
  private String arn;

  @Column(name = "application_id")
  private Long applicationId;

  @Column(name = "avatar_type")
  @Enumerated(EnumType.STRING)
  private AvatarType avatarType;

  @Column(name = "topic_subscriptions", columnDefinition = "TEXT")
  private String topicSubscriptions;

  public Token(String value, int environment, int type, ClientAgentCity agent, User user, AvatarType avatarType) {
    this.value = value;
    this.environment = environment;
    this.type = type;
    this.agent = agent;
    this.user = user;
    this.avatarType = avatarType;
  }

  public TokenType getType() {
    return TokenType.parse(this.type);
  }

  public void setType(TokenType type) {
    this.type = type.getValue();
  }

  public void setType(int type) {
    this.type = type;
  }

  public TokenEnvironment getEnvironment() {
    return TokenEnvironment.parse(this.environment);
  }

  public void setEnvironment(TokenEnvironment environment) {
    this.environment = environment.getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}