package com.rideaustin.driverstatistic.model;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = DriverStatisticRepository.class)
public class DriverStatisticConfig {
}
