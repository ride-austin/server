package com.rideaustin.repo.dsl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.QCarType;
import com.rideaustin.model.ride.QCityCarType;

@Repository
public class CarTypeDslRepository extends AbstractDslRepository {

  private static final QCarType qCarType = QCarType.carType;
  private static final QCityCarType qCityCarType = QCityCarType.cityCarType;

  public List<CarType> getAllOrdered() {
    return buildQuery(qCarType)
      .where(qCarType.active.isTrue())
      .orderBy(qCarType.order.asc())
      .fetch();
  }

  public CarType findByCategory(String category) {
    return buildQuery(qCarType)
      .where(qCarType.carCategory.eq(category))
      .fetchOne();
  }

  public Optional<CityCarType> findByTypeAndCity(CarType carType, Long cityId) {
    return Optional.ofNullable(buildQuery(qCityCarType)
      .where(
        qCityCarType.carType.eq(carType),
        qCityCarType.cityId.eq(cityId)
      ).fetchOne());
  }

  public Optional<CityCarType> findByTypeAndCity(String carType, Long cityId) {
    return Optional.ofNullable(buildQuery(qCityCarType)
      .where(
        qCityCarType.carType.carCategory.eq(carType),
        qCityCarType.cityId.eq(cityId)
      ).fetchOne());
  }

}
