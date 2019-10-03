package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.airports.QAirport;
import com.rideaustin.service.airport.AirportCache;

@Repository
public class AirportDslRepository extends AbstractDslRepository {

  private static final QAirport qAirport = QAirport.airport;

  @Cacheable(AirportCache.AIRPORTS_CACHE)
  public List<Airport> findAll() {
    return buildQuery(qAirport)
      .where(qAirport.enabled.isTrue())
      .orderBy(qAirport.id.asc())
      .fetch();
  }

}
