package com.rideaustin.repo.dsl;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.user.ApiClient;
import com.rideaustin.model.user.QApiClient;

@Repository
public class ApiClientDslRepository extends AbstractDslRepository {

  private static final QApiClient qApiClient = QApiClient.apiClient;

  public ApiClient findByEmail(String email) {
    return buildQuery(qApiClient)
      .where(qApiClient.user.email.eq(email))
      .fetchOne();
  }
}
