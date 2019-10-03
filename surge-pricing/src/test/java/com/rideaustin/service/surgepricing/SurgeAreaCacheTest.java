package com.rideaustin.service.surgepricing;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

public class SurgeAreaCacheTest {

  @Mock
  private SurgeAreaDslRepository surgeAreaDslRepository;
  @Mock
  private SurgeAreaRedisRepository surgeAreaRedisRepository;
  @Mock
  private CarTypesCache carTypesCache;

  private SurgeAreaCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    CarTypesUtils.setCarTypesCache(carTypesCache);

    testedInstance = new SurgeAreaCache(surgeAreaDslRepository, surgeAreaRedisRepository);
  }

  @Test
  public void refreshCacheRefreshesWhenForced() {
    final SurgeArea surgeArea = new SurgeArea();
    final AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("-97.64613,34.98161 -97.661531,34.6981681 -97.646131,34.3168131");
    surgeArea.setAreaGeometry(areaGeometry);
    when(surgeAreaDslRepository.findAllActive()).thenReturn(Collections.singletonList(surgeArea));

    testedInstance.refreshCache(true);

    verify(surgeAreaRedisRepository).deleteAll();
    verify(surgeAreaDslRepository).findAllActive();
    verify(surgeAreaRedisRepository).save(any(RedisSurgeArea.class));
  }
}