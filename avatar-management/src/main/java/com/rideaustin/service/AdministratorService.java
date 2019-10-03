package com.rideaustin.service;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.AdministratorDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;

@Service
@Transactional
public class AdministratorService {

  private final AdministratorDslRepository administratorDslRepository;

  @Inject
  public AdministratorService(AdministratorDslRepository administratorDslRepository) {
    this.administratorDslRepository = administratorDslRepository;
  }

  @Nonnull
  public Administrator findAdministrator(long id) throws RideAustinException {
    Administrator administrator = administratorDslRepository.findById(id);
    if (administrator == null) {
      throw new NotFoundException("Administrator not found");
    }
    return administrator;
  }

  @Nonnull
  public Administrator findAdministrator(User user) throws RideAustinException {
    Administrator administrator = administratorDslRepository.findByUser(user);
    if (administrator == null) {
      throw new NotFoundException("Administrator not found");
    }
    return administrator;
  }
}
