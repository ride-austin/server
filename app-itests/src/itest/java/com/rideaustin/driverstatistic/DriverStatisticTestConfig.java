package com.rideaustin.driverstatistic;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DriverStatisticTestConfig {


  @Bean
  RideAcceptedByDriverEventInterceptor rideAcceptedByDriverEventInterceptor() {
    return new RideAcceptedByDriverEventInterceptor();
  }

}
