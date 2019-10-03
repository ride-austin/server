package com.rideaustin.repo;

import java.util.Date;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.PasswordVerificationToken;
import com.rideaustin.model.QPasswordVerificationToken;
import com.rideaustin.repo.dsl.AbstractDslRepository;

@Repository
public class PasswordVerificationTokenDslRepository extends AbstractDslRepository {

  private static final QPasswordVerificationToken qToken = QPasswordVerificationToken.passwordVerificationToken;

  public PasswordVerificationToken findToken(String token) {
    return buildQuery(qToken)
      .where(
        qToken.token.eq(token),
        qToken.expiresOn.after(new Date())
      ).fetchOne();
  }
}
