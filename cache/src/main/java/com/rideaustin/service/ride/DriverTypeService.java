package com.rideaustin.service.ride;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.DriverTypeCache;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverTypeService {

  private final DriverTypeCache driverTypeCache;
  private final CarTypesCache carTypesCache;

  public Map<String, DriverType> getAll() {
    return driverTypeCache.getDriverTypes();
  }

  public DriverType getOne(String driverType) {
    return driverTypeCache.getDriverType(driverType);
  }

  public boolean checkIfDriverTypesSupportCarCategories(Set<String> carCategory, Set<String> driverTypes, Long cityId) {
    boolean eligibleByDriverTypes = true;
    for (String driverType : driverTypes) {
      DriverType driverTypeObject = driverTypeCache.getDriverType(driverType);
      CityDriverType cityDriverType = driverTypeObject.getCityDriverTypes().stream().filter(cd -> cd.getCityId().equals(cityId)).findFirst().get();

      eligibleByDriverTypes = carTypesCache.fromBitMask(cityDriverType.getCarCategoriesBitmask()).containsAll(carCategory);
      if (!eligibleByDriverTypes) {
        break;
      }
    }
    return eligibleByDriverTypes;
  }

  public List<CityDriverType> getCityDriverTypes(Long cityId) {
    if (CollectionUtils.isNotEmpty(driverTypeCache.getCityDriverTypes(cityId))) {
      return driverTypeCache.getCityDriverTypes(cityId).stream().filter(dt -> dt.isEnabled() && dt.getDriverType().isEnabled()).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  public Optional<CityDriverType> getCityDriverType(Integer driverType, Long cityId) {
    return getCityDriverTypes(cityId).stream().filter(ct -> ct.getDriverType().getBitmask().equals(driverType)).findFirst();
  }

  public boolean isDriverTypeExist(String driverType) {
    return !MapUtils.isEmpty(driverTypeCache.getDriverTypes()) && driverTypeCache.getDriverTypes().keySet().contains(driverType);
  }

  public boolean isCitySupportDriverType(String driverType, Long cityId) {
    DriverType dt = driverTypeCache.getDriverType(driverType);
    if (dt != null) {
      Optional<CityDriverType> cdt = dt.getCityDriverTypes().stream().filter(d -> d.getCityId().equals(cityId)).findFirst();
      return cdt.isPresent() && cdt.get().isEnabled() && cdt.get().getDriverType().isEnabled();
    }
    return false;
  }

}