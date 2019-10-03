package com.rideaustin.service.ride;

import javax.inject.Inject;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.jpa.RideRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Repository
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideLoadService {

  private final RideRepository rideRepository;

  @Retryable(value = { Exception.class }, maxAttempts = 5, backoff = @Backoff(delay = 1000)) // 1s
  @Transactional(noRollbackFor = Exception.class)
  public Ride findOneForUpdateWithRetry(Long id) {
    Ride ride;
    try {
      ride = rideRepository.findOneForUpdate(id);
    } catch (Exception e) {      
      log.error("Retrying loading ride for update.");
      throw e;
    }
    return ride;
  }

}
