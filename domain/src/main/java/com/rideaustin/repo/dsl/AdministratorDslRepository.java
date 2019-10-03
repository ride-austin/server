package com.rideaustin.repo.dsl;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.QAdministrator;
import com.rideaustin.model.user.User;

@Repository
public class AdministratorDslRepository extends AbstractDslRepository {

  private static final QAdministrator qAdministrator = QAdministrator.administrator;

  public Administrator findById(Long id) {
    return buildQuery(qAdministrator)
      .where(qAdministrator.id.eq(id))
      .fetchOne();
  }

  public Administrator findByUser(User user) {
    return buildQuery(qAdministrator)
      .where(qAdministrator.user.eq(user))
      .fetchOne();
  }
}
