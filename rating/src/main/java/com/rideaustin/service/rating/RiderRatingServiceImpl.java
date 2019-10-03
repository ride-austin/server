package com.rideaustin.service.rating;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RiderDslRepository;

@Service
public class RiderRatingServiceImpl extends AbstractRatingService<Rider> implements RiderRatingService {

  private RiderDslRepository riderDslRepository;

  @Inject
  public RiderRatingServiceImpl(ConfigurationItemCache configurationItemCache, RiderDslRepository riderDslRepository) {
    super(configurationItemCache, ClientType.RIDER);
    this.riderDslRepository = riderDslRepository;
  }

  @Override
  public Double calculateRating(Rider rider) {
    return riderDslRepository.findRatingAverage(this.getDefaultRating(), this.getMinimumRatingThreshold(), this.getLimit(), rider);
  }
}
