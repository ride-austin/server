package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.QTerms;
import com.rideaustin.model.QTermsAcceptance;
import com.rideaustin.model.QTermsInfo;
import com.rideaustin.model.Terms;
import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.model.TermsInfo;

@Repository
public class TermsDslRepository extends AbstractDslRepository {

  private static final QTermsAcceptance qTermsAcceptance = QTermsAcceptance.termsAcceptance;
  private static final QTerms qTerms = QTerms.terms;

  public TermsInfo getCurrent(Long cityId) {
    return buildQuery(qTerms)
      .select(new QTermsInfo(qTerms.id, qTerms.url, qTerms.mandatory, qTerms.version, qTerms.publicationDate))
      .where(
        qTerms.current.isTrue(),
        qTerms.cityId.eq(cityId)
      )
      .fetchOne();
  }

  public Terms getOne(Long termId) {
    return get(termId, Terms.class);
  }

  public TermsAcceptance getTermsAcceptance(Long driverId, Long termId) {
    return buildQuery(qTermsAcceptance)
      .where(
        qTermsAcceptance.driver.id.eq(driverId),
        qTermsAcceptance.terms.id.eq(termId)
      )
      .fetchOne();
  }

  public TermsAcceptance getCurrentTermAcceptance(Long driverId, Long cityId) {
    return buildQuery(qTermsAcceptance)
      .where(
        qTermsAcceptance.driver.id.eq(driverId),
        qTermsAcceptance.terms.current.isTrue(),
        qTerms.cityId.eq(cityId)
      )
      .fetchOne();
  }

  public List<TermsAcceptance> getCurrentTermAcceptances(List<Long> driverIds, Long cityId) {
    return buildQuery(qTermsAcceptance)
      .where(
        qTermsAcceptance.driver.id.in(driverIds),
        qTermsAcceptance.terms.current.isTrue(),
        qTerms.cityId.eq(cityId)
      )
      .fetch();
  }
}
