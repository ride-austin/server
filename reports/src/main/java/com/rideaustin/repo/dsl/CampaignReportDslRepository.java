package com.rideaustin.repo.dsl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.QCampaign;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.report.entry.CampaignStatsReportEntry;
import com.rideaustin.report.entry.CampaignTripReportEntry;
import com.rideaustin.report.entry.QCampaignStatsReportEntry;
import com.rideaustin.report.entry.QCampaignTripReportEntry;
import com.rideaustin.service.model.BoundingBox;
import com.rideaustin.utils.GeometryUtils;

@Repository
public class CampaignReportDslRepository extends AbstractDslRepository {

  private static final QCampaign qCampaign = QCampaign.campaign;
  private static final QRide qRide = QRide.ride;

  public List<CampaignTripReportEntry> getCampaignRides(long id, Date from, Date to) {
    List<Long> ids = getRideIds(id);
    return buildQuery(qRide)
      .where(
        qRide.createdDate.between(from, to),
        qRide.id.in(ids)
      )
      .orderBy(qRide.createdDate.asc())
      .select(new QCampaignTripReportEntry(qRide.createdDate, qRide.start.address, qRide.startLocationLat, qRide.startLocationLong,
        qRide.end.address, qRide.endLocationLat, qRide.endLocationLong, qRide.distanceTravelled, qRide.driverAcceptedOn,
        qRide.startedOn, qRide.completedOn, qRide.fareDetails, qRide.activeDriver.driver.user.firstname,
        qRide.activeDriver.driver.user.lastname, qRide.activeDriver.driver.user.email, qRide.rider.user.firstname,
        qRide.rider.user.lastname, qRide.rider.user.email, qRide.driverRating, qRide.riderRating))
      .fetch();
  }

  public List<CampaignStatsReportEntry> getCampaignStats(long id, Date startDate, Date endDate) {
    final Campaign campaign = buildQuery(qCampaign)
      .where(qCampaign.id.eq(id))
      .fetchOne();
    if (campaign != null) {
      final Set<CampaignArea> zones = campaign.getDropoffZones();
      zones.addAll(campaign.getPickupZones());
      final List<BoundingBox> boundingBoxes = zones.stream()
        .map(CampaignArea::getArea)
        .map(AreaGeometry::getCsvGeometry)
        .map(GeometryUtils::buildCoordinates)
        .map(GeometryUtils::getBoundingBox)
        .collect(Collectors.toList());
      BooleanBuilder booleanBuilder = new BooleanBuilder();
      for (BoundingBox boundingBox : boundingBoxes) {
        booleanBuilder.or(
          qRide.startLocationLat.between(boundingBox.getBottomRightCorner().lat, boundingBox.getTopLeftCorner().lat)
            .and(qRide.startLocationLong.between(boundingBox.getBottomRightCorner().lng, boundingBox.getTopLeftCorner().lng))
        );
      }
      final Set<Integer> weekdays = ConfigurationWeekday.fromBitmask(campaign.getActiveOnDays())
        .stream()
        .map(ConfigurationWeekday::getWeekDay)
        .collect(Collectors.toSet());
      return buildQuery(qRide)
        .select(new QCampaignStatsReportEntry(qRide.status, qRide.count()))
        .where(
          qRide.createdDate.between(startDate, endDate),
          new BooleanBuilder()
            .or(
              booleanBuilder.and(
                qRide.createdDate.hour().between(campaign.getActiveFromHour(), campaign.getActiveToHour()))
                .and(
                  qRide.createdDate.dayOfWeek().in(weekdays)
                )
                .and(
                  qRide.status.in(RideStatus.NO_AVAILABLE_DRIVER, RideStatus.RIDER_CANCELLED)
                )
            )
            .or(
              qRide.id.in(getRideIds(id))
            )
        )
        .groupBy(qRide.status)
        .fetch();
    } else {
      return Collections.emptyList();
    }
  }

  private List<Long> getRideIds(long id) {
    final List ridesRaw = buildQuery(qCampaign)
      .where(qCampaign.id.eq(id))
      .select(qCampaign.rides)
      .fetch();
    return (List<Long>) ridesRaw.stream().map(o -> ((BaseEntity) o).getId()).collect(Collectors.toList());
  }
}
