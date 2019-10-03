package com.rideaustin.service.rating;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;

@Service
public class DriverRatingServiceImpl extends AbstractRatingService<Driver> implements DriverRatingService {

  private final DriverDslRepository driverDslRepository;

  @Inject
  public DriverRatingServiceImpl(ConfigurationItemCache configurationItemCache, DriverDslRepository driverDslRepository) {
    super(configurationItemCache, ClientType.DRIVER);
    this.driverDslRepository = driverDslRepository;
  }

  @Override
  public Double calculateRating(Driver driver) {
    return driverDslRepository.findRatingAverage(this.getDefaultRating(), this.getMinimumRatingThreshold(), this.getLimit(), driver);
  }
}
