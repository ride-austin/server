package com.rideaustin.repo.jpa;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.google.common.base.CaseFormat;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.model.DriverRidesReportEntry;

@Repository
public class DriverRidesReportRepository {

  private static final String FROM = "from rides r " +
          "inner join active_drivers ad on ad.id = r.active_driver_id " +
          "inner join avatars a on a.id = ad.driver_id " +
          "inner join users u on u.id = a.user_id " +
          "inner join city_car_types ct on ct.car_category = r.requested_car_category and ct.city_id = r.city_id " +
          "left join (" +
          "   select count(r.id) c, ad.driver_id " +
          "   from rides r " +
          "   inner join active_drivers ad on ad.id = r.active_driver_id " +
          "   where " +
          "     r.status = 'COMPLETED' " +
          "     and (r.completed_on between :startDate and :endDate) " +
          "     and (:zipCode IS NULL OR r.start_zip_code = :zipCode OR r.end_zip_code = :zipCode) AND (:cityId IS NULL OR r.city_id = :cityId)" +
          "     and surge_factor > 1 " +
          "   group by ad.driver_id" +
          ") pf on pf.driver_id = a.id " +
          "left join ( " +
          "   select ad.driver_id, sum(r.cancellation_fee) as cancellation_fee " +
          "   from rides r " +
          "   inner join active_drivers ad on ad.id = r.active_driver_id " +
          "   where " +
          "     r.cancelled_on between :startDate and :endDate " +
          "     and r.cancellation_fee is not null " +
          "     and (:zipCode is null or r.start_zip_code = :zipCode or r.end_zip_code = :zipCode) AND (:cityId IS NULL OR r.city_id = :cityId)" +
          "   group by ad.driver_id " +
          ") cr on cr.driver_id = a.id " +
          "WHERE ((r.status = 'COMPLETED' AND r.completed_on BETWEEN :startDate AND :endDate) " +
          "       OR (r.status LIKE '%%_CANCELLED' AND (r.cancellation_fee IS NOT NULL OR r.total_fare IS NOT NULL) AND " +
          "           r.cancelled_on BETWEEN :startDate AND :endDate)) AND " +
          "      (:zipCode IS NULL OR r.start_zip_code = :zipCode OR r.end_zip_code = :zipCode) AND (:cityId IS NULL OR r.city_id = :cityId)" +
          "group by a.id ";

  private static final String QUERY = "SELECT%n" +
          "  a.id                                                            \"driver_id\",%n" +
          "  u.id                                                            \"user_id\",%n" +
          "  u.first_name,%n" +
          "  u.last_name,%n" +
          "  count(CASE r.status%n" +
          "        WHEN 'COMPLETED'%n" +
          "          THEN 1%n" +
          "        ELSE NULL END)                                            \"completed_rides\",%n" +
          "  sum(ifnull(pf.c, 0))                                                \"priority_fare_rides\",%n" +
          "  sum(ifnull(r.distance_travelled, 0))                            \"distance_travelled\",%n" +
          "  sum(ifnull(r.distance_travelled, 0))                            \"distance_traveled_in_miles\",%n" +
          "  sum(if(r.normal_fare IS NULL, ifnull(r.driver_payment, 0) - ifnull(r.tip, 0),%n" +
          "         round(r.normal_fare * (1 - ct.ra_fee_factor), 2)))       \"driver_base_payment\",%n" +
          "  sum(ifnull(r.tip, 0))                                           \"tips\",%n" +
          "  sum(round(ifnull(r.surge_fare, 0) * (1 - ct.ra_fee_factor), 2)) \"priority_fare\",%n" +
          "  sum(ifnull(cr.cancellation_fee, 0))                                  \"cancellation_fee\",%n" +
          "  sum(ifnull(r.driver_payment, 0))                                \"driver_payment\",%n" +
          "  sum(ifnull(r.ra_payment, 0))                                    \"ra_gross_margin\",%n" +
          "  sum(ifnull(r.total_fare, 0))                                    \"total_fare\"" +
          FROM +
          "order by %s %s ";

  private static final String COUNT_QUERY = "select count(*) as cnt from (select 1 " + FROM + ") c";

  @PersistenceContext(unitName = "RideAustinPC")
  private EntityManager em;

  public Page<DriverRidesReportEntry> driverRideReport(Date startDate, Date endDate, String zipCode, Long cityId, PagingParams pagingParams) {
    String sort = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, pagingParams.getSort().size() == 1 ? pagingParams.getSort().get(0) : "driver_id");
    String query = String.format(QUERY, sort, pagingParams.isDesc() ? "desc" : "asc");
    Query report = em.createNativeQuery(query, "DriverRidesReportResult");
    setParameters(report, startDate, endDate, zipCode, cityId);
    report.setFirstResult(pagingParams.getPage() * pagingParams.getPageSize());
    report.setMaxResults(pagingParams.getPageSize());

    @SuppressWarnings("unchecked")
    List<DriverRidesReportEntry> rawContent = report.getResultList();
    Query total = em.createNativeQuery(COUNT_QUERY);
    setParameters(total, startDate, endDate, zipCode, cityId);

    return new PageImpl<>(rawContent, pagingParams.toPageRequest(), ((BigInteger) total.getSingleResult()).longValue());
  }

  private void setParameters(Query query, Date startDate, Date endDate, String zipCode, Long cityId) {
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    query.setParameter("zipCode", zipCode);
    query.setParameter("cityId", cityId);
  }

}