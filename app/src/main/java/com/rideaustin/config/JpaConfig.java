package com.rideaustin.config;

import java.sql.Connection;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.rideaustin.model.user.User;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableTransactionManagement(order = 1000)
@EnableJpaAuditing(auditorAwareRef = "auditor")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@EnableJpaRepositories(basePackages = "com.rideaustin.repo.jpa")
public class JpaConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(JpaConfig.class);

  private final Environment env;
  private final DataSource dataSource;

  private static final String HIBERNATE_DIALECT = "hibernate.dialect";
  private static final String PERSISTENCE_UNIT = "RideAustinPC";

  @Bean(initMethod = "migrate")
  @Profile("!itest")
  public Flyway flyway() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    flyway.setLocations("classpath:db/migration", "classpath:com/rideaustin/migration");
    return flyway;
  }

  @Bean(initMethod = "migrate", name = "flyway")
  @Profile("itest")
  public Flyway flywayItest() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    flyway.setLocations("classpath:db/migration", "classpath:com/rideaustin/migration", "classpath:com/rideaustin/test/migration");
    return flyway;
  }

  @Bean
  @DependsOn("flyway")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setDataSource(dataSource);
    emf.setPackagesToScan(packagesToScan());
    emf.setPersistenceUnitName(PERSISTENCE_UNIT);
    emf.setPersistenceProvider(new HibernatePersistenceProvider());
    emf.setJpaProperties(jpaProperties());
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    return emf;
  }

  private Properties jpaProperties() {
    Properties extraProperties = new Properties();
    extraProperties.put("hibernate.connection.release_mode", env.getProperty("hibernate.connection.release_mode"));
    extraProperties.put("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
    extraProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
    extraProperties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "validate"));
    extraProperties.put("hibernate.connection.isolation", Connection.TRANSACTION_READ_COMMITTED);
    extraProperties.put("hibernate.jdbc.batch_size", env.getProperty("hibernate.jdbc.batch_size", Integer.class, 20));
    extraProperties.put("hibernate.order_inserts", env.getProperty("hibernate.order_inserts", Boolean.class, true));
    extraProperties.put("hibernate.order_updates", env.getProperty("hibernate.order_updates", Boolean.class, true));
    extraProperties.put("hibernate.jdbc.batch_versioned_data", env.getProperty("hibernate.jdbc.batch_versioned_data", Boolean.class, false));

    LOGGER.debug("hibernate.dialect @{}", env.getProperty(HIBERNATE_DIALECT));
    if (env.getProperty(HIBERNATE_DIALECT) != null) {
      extraProperties.put(HIBERNATE_DIALECT, env.getProperty(HIBERNATE_DIALECT));
    }
    return extraProperties;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    LOGGER.debug("CREATING EMF ...");
    return new JpaTransactionManager(entityManagerFactory().getObject());
  }

  @Bean
  public AuditorAware<User> auditor() {
    return () -> null;
  }

  protected String[] packagesToScan() {
    return new String[]{
      "com.rideaustin"
    };
  }
}