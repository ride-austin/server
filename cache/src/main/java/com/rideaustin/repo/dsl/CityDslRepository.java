package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.City;
import com.rideaustin.model.QCity;

@Repository
public class CityDslRepository extends AbstractDslRepository {

  private static final QCity qCity = QCity.city;

  public List<City> findAll() {
    return buildQuery(qCity).fetch();
  }

  public List<City> findAllEnabled() {
    return buildQuery(qCity).where(qCity.enabled.eq(Boolean.TRUE)).fetch();
  }

}
