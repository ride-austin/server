package com.rideaustin.config;

import org.joda.money.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.ApiClient;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.utils.MoneyDeserializer;
import com.rideaustin.utils.MoneySerializer;

@Configuration
public class JsonDataMapperConfig {
  public interface PageMixin<T> {
    @JsonIgnore
    Sort getSort();
  }

  @Bean(name = "objectMapper")
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, false);
    objectMapper.addMixIn(Page.class, PageMixin.class);

    SimpleModule raModule = new SimpleModule("RideAustinModule", new Version(1, 0, 0, null, null, null));
    raModule.addSerializer(Money.class, new MoneySerializer());
    raModule.addDeserializer(Money.class, new MoneyDeserializer());
    raModule.registerSubtypes(
      new NamedType(Rider.class, AvatarType.NAME_RIDER),
      new NamedType(Driver.class, AvatarType.NAME_DRIVER),
      new NamedType(Administrator.class, AvatarType.NAME_ADMIN),
      new NamedType(ApiClient.class, AvatarType.NAME_API_CLIENT)
    );
    objectMapper.registerModule(raModule);
    objectMapper.registerModule(new JavaTimeModule());

    return objectMapper;
  }
}
