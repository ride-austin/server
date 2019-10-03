package com.rideaustin.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DataSourceConfig {

  private final Environment environment;

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);

  private static final String MYSQL_PREP_STMT_CACHE_SIZE = "250";
  private static final String MYSQL_PREP_STMT_CACHE_SQL_LIMIT = "2048";
  private static final String MYSQL_TEST_CONNECTION_QUERY = "SELECT 1 FROM DUAL";

  private static final int MAXIMUM_POOL_SIZE = 25;       // maximum number of connections
  private static final int CONNECTION_TIMEOUT = 30000;   // maximum number of milliseconds waiting for a connection from the pool
  private static final int LEAK_DETECTION_THRESHOLD = 0; // amount of time that a connection can be out of the pool
  private static final String USE_LOCAL_TRANSACTION_STATE = "true";
  private static final String USE_LOCAL_SESSION_STATE = "true";

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {

    LOGGER.debug("BUILDING DATA SOURCE ...");

    HikariConfig hikariConfig = new HikariConfig();

    hikariConfig.setPoolName("RideAustin-cp");

    hikariConfig.setJdbcUrl(environment.getProperty("jdbc.url"));
    hikariConfig.setUsername(environment.getProperty("jdbc.username"));
    hikariConfig.setPassword(environment.getProperty("jdbc.password"));
    hikariConfig.setDriverClassName(environment.getProperty("jdbc.driverClassName"));

    hikariConfig.setConnectionTestQuery(MYSQL_TEST_CONNECTION_QUERY);

    hikariConfig.addDataSourceProperty("useUnicode", "true");
    hikariConfig.addDataSourceProperty("characterEncoding", "utf-8");
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
    hikariConfig.addDataSourceProperty("useLocalTransactionState", environment.getProperty("jdbc.pool.useLocalTransactionState", USE_LOCAL_TRANSACTION_STATE));
    hikariConfig.addDataSourceProperty("useLocalSessionState", environment.getProperty("jdbc.pool.useLocalSessionState", USE_LOCAL_SESSION_STATE));
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", MYSQL_PREP_STMT_CACHE_SIZE);
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", MYSQL_PREP_STMT_CACHE_SQL_LIMIT);

    hikariConfig.setMaximumPoolSize(environment.getProperty("jdbc.pool.maximumPoolSize", Integer.class, MAXIMUM_POOL_SIZE));
    hikariConfig.setConnectionTimeout(environment.getProperty("jdbc.pool.connectionTimeout", Integer.class, CONNECTION_TIMEOUT));
    hikariConfig.setLeakDetectionThreshold(environment.getProperty("jdbc.pool.leakDetectionThreshold", Integer.class, LEAK_DETECTION_THRESHOLD));

    final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
    LOGGER.debug(String.format("RideAustin-cp was successfully set: %s", hikariDataSource));

    return hikariDataSource;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}