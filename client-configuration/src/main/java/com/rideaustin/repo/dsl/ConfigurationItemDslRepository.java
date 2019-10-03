package com.rideaustin.repo.dsl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.QConfigurationItem;

@Repository
public class ConfigurationItemDslRepository extends AbstractDslRepository {

  private static final QConfigurationItem qConfigurationItem = QConfigurationItem.configurationItem;

  public List<ConfigurationItem> findAll(String environment) {
    return buildQuery(qConfigurationItem)
      .where(qConfigurationItem.environment.eq(environment)
        .or(qConfigurationItem.environment.isNull()))
      .orderBy(qConfigurationItem.id.asc())
      .fetch();
  }

  public ConfigurationItem findByKeyAndCityId(String key, Long cityId) {
    return buildQuery(qConfigurationItem)
      .where(
        qConfigurationItem.configurationKey.eq(key),
        qConfigurationItem.cityId.eq(cityId)
      )
      .fetchOne();
  }

  public ConfigurationItem findByKeyClientAndCity(String key, ClientType clientType, Long cityId) {
    return buildQuery(qConfigurationItem)
      .where(
        qConfigurationItem.configurationKey.eq(key),
        qConfigurationItem.cityId.eq(cityId),
        qConfigurationItem.clientType.eq(clientType)
      )
      .fetchOne();
  }

  public ConfigurationItem findByKey(String key, String environment) {
    return buildQuery(qConfigurationItem)
      .where(qConfigurationItem.configurationKey.eq(key)
        .andAnyOf(qConfigurationItem.environment.eq(environment), qConfigurationItem.environment.isNull()))
      .fetchOne();
  }

  public void remove(Long id) {
    Objects.requireNonNull(id);

    ConfigurationItem entity = findOne(id);

    if (entity != null) {
      entityManager.remove(entity);
    }
  }

  public ConfigurationItem findOne(Long id) {
    Objects.requireNonNull(id);
    return get(id, ConfigurationItem.class);
  }
}
