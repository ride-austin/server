package com.rideaustin.service.ride;

import static com.rideaustin.Constants.NEUTRAL_SURGE_FACTOR;
import static com.rideaustin.utils.FormatUtils.formatDate;
import static com.rideaustin.utils.FormatUtils.formatDuration;
import static com.rideaustin.utils.FormatUtils.formatMoneyAmount;
import static com.rideaustin.utils.FormatUtils.formatTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.joda.money.MoneyUtils;

import com.rideaustin.Constants;
import com.rideaustin.model.Charity;
import com.rideaustin.model.City;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.email.AbstractPaymentTemplateEmail;
import com.rideaustin.service.model.CampaignDto;
import com.rideaustin.service.model.FarePaymentDto;
import com.rideaustin.utils.FormatUtils;

public class EndRideEmail extends AbstractPaymentTemplateEmail {

  public static final String TITLE = Constants.EmailTitle.END_RIDE_EMAIL;
  private static final String TEMPLATE = "rider_receipt.ftl";
  private static final String FULL_NAME_FORMAT = "%s %s";

  public EndRideEmail(@Nonnull Ride ride, @Nonnull Rider rider, @Nonnull City city, @Nonnull FarePaymentDto riderPayment,
    @Nonnull byte[] imageData, String driverPhoto, CampaignDto campaign) throws EmailException {
    super(TITLE.concat(city.getAppName()), TEMPLATE);
    setModel(createRideDataModel(ride, rider, riderPayment, null, city, null, driverPhoto, campaign));
    embed(new ByteArrayDataSource(imageData, "image/png"), String.format("routeMap%d.png", ride.getId()), String.format("routemap%d", ride.getId()));
    addRecipient(rider.getFullName(), rider.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

  public EndRideEmail(@Nonnull Ride ride, @Nonnull Rider rider, @Nonnull City city, @Nonnull FarePaymentDto riderPayment,
    List<FarePaymentDto> farePaymentsForParticipants, @Nonnull byte[] imageData,
    Promocode promocode, String driverPhoto, CampaignDto campaign) throws EmailException {
    super(TITLE.concat(city.getAppName()), TEMPLATE);
    setModel(createRideDataModel(ride, rider, riderPayment, farePaymentsForParticipants, city, promocode, driverPhoto, campaign));
    embed(new ByteArrayDataSource(imageData, "image/png"), String.format("routeMap%d.png", ride.getId()), String.format("routemap%d", ride.getId()));
    addRecipient(rider.getFullName(), rider.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

  private Map<String, Object> createRideDataModel(@Nonnull Ride ride, @Nonnull Rider rider, @Nonnull FarePaymentDto riderPayment,
    List<FarePaymentDto> farePaymentsForParticipants, @Nonnull City city, @Nullable Promocode promocode,
    String driverPhoto, CampaignDto campaign) {
    Map<String, Object> dataModel = new HashMap<>();
    dataModel.put("fare", formatMoneyAmount(ride.getNormalFare()));
    dataModel.put("rideId", String.valueOf(ride.getId()));
    dataModel.put("rideDate", formatDate(ride.getCreatedDate()));
    dataModel.put("riderName", rider.isDispatcherAccount() ?
      String.format(FULL_NAME_FORMAT, ride.getRiderOverride().getFirstName(), ride.getRiderOverride().getLastName()) :
      "You");
    dataModel.put("baseFare", formatMoneyAmount(ride.getBaseFare()));
    dataModel.put("distanceFare", formatMoneyAmount(ride.getDistanceFare()));
    dataModel.put("timeFare", formatMoneyAmount(ride.getTimeFare()));
    dataModel.put("totalFare", formatMoneyAmount(ride.getTotalFare()));
    dataModel.put("subTotal", formatMoneyAmount(ride.getSubTotal()));
    boolean hasOnlyMinimumFare = ride.getSubTotal().isEqual(ride.getMinimumFare()) || ride.getSubTotal().isLessThan(ride.getMinimumFare());
    dataModel.put("hasOnlyMinimumFare", hasOnlyMinimumFare);
    boolean hasBookingFee = MoneyUtils.isPositive(ride.getBookingFee());
    dataModel.put("hasBookingFee", hasBookingFee);
    if (hasBookingFee) {
      dataModel.put("bookingFee", formatMoneyAmount(ride.getBookingFee()));
    }
    boolean hasAirportFee = MoneyUtils.isPositive(ride.getAirportFee());
    dataModel.put("hasAirportFee", hasAirportFee);
    if (hasAirportFee) {
      dataModel.put("airportFee", formatMoneyAmount(ride.getAirportFee()));
    }
    dataModel.put("cityFee", formatMoneyAmount(ride.getCityFee()));
    dataModel.put("rideCost", formatMoneyAmount(ride.getRideCost()));
    boolean hasProcessingFee = MoneyUtils.isPositive(ride.getProcessingFee());
    dataModel.put("hasProcessingFee", hasProcessingFee);
    if (hasProcessingFee) {
      dataModel.put("processingFee", formatMoneyAmount(ride.getProcessingFee()));
    }
    dataModel.put("startTime", formatTime(ride.getStartedOn()));
    dataModel.put("endTime", formatTime(ride.getCompletedOn()));
    String startLocation =
      StringUtils.isNotBlank(ride.getStart().getAddress()) ? ride.getStart().getAddress() :
        String.format(FULL_NAME_FORMAT, ride.getStartLocationLat(), ride.getStartLocationLong());
    dataModel.put("startLocation", startLocation);
    String endLocation =
      StringUtils.isNotBlank(ride.getEnd().getAddress()) ? ride.getEnd().getAddress() :
        String.format(FULL_NAME_FORMAT, ride.getEndLocationLat(), ride.getEndLocationLong());
    dataModel.put("endLocation", endLocation);
    dataModel.put("car", ride.getRequestedCarType().getTitle());
    dataModel.put("tripTime", formatDuration(ride.getStartedOn(), ride.getCompletedOn(), "m\'m \'s\'s\'")); //"H\'h, \'m\'m, \'s\'s\'"
    dataModel.put("distanceTravelled", FormatUtils.formatDecimal(ride.getDistanceTravelledInMiles()));
    Driver driver = ride.getActiveDriver().getDriver();
    dataModel.put("driverNickName", StringUtils.defaultIfBlank(driver.getUser().getNickName(), driver.getFirstname()));
    if (StringUtils.isNoneEmpty(driverPhoto)) {
      dataModel.put("driverPhoto", driverPhoto);
    } else {
      // Empty photo
      dataModel.put("driverPhoto", Constants.DEFAULT_DRIVER_PHOTO);
    }
    boolean hasSurge = ride.getSurgeFactor() != null &&
      NEUTRAL_SURGE_FACTOR.compareTo(ride.getSurgeFactor()) != 0;
    dataModel.put("hasSurge", hasSurge);
    if (hasSurge) {
      dataModel.put("normalFare", formatMoneyAmount(ride.getNormalFare()));
      dataModel.put("surgeFare", formatMoneyAmount(ride.getSurgeFare()));
      dataModel.put("surgeFactor", FormatUtils.formatDecimal(ride.getSurgeFactor()));
    }

    insertPaymentMethodInformation(dataModel, riderPayment);

    // Check for charity
    Charity charity = ride.getCharity();
    boolean hasCharity = charity != null && MoneyUtils.isPositive(ride.getRoundUpAmount());
    dataModel.put("hasCharity", hasCharity);
    if (hasCharity) {
      dataModel.put("roundUpFare", formatMoneyAmount(ride.getRoundUpAmount()));
      dataModel.put("charityLogo", charity.getImageUrl());
      dataModel.put("charityName", charity.getName());
    }
    // Check for charity
    boolean tipped = ride.getTip() != null && riderPayment.isMainRider();
    dataModel.put("isTipped", tipped);
    if (tipped) {
      dataModel.put("tip", formatMoneyAmount(ride.getTip()));
    }

    // Check for free credit used for main driver
    dataModel.put("hasPromocode", MoneyUtils.isPositive(riderPayment.getFreeCreditCharged()));
    boolean hasRideCredit = promocode != null && MoneyUtils.isPositive(ride.getFreeCreditCharged()) && promocode.isApplicableToFees();
    dataModel.put("hasRideCredit", hasRideCredit);
    boolean hasFareCredit = promocode != null && MoneyUtils.isPositive(ride.getFreeCreditCharged()) && !promocode.isApplicableToFees();
    dataModel.put("hasFareCredit", hasFareCredit);
    if (hasRideCredit || hasFareCredit) {
      dataModel.put("freeCreditCharge", formatMoneyAmount(riderPayment.getFreeCreditCharged()));
    }
    if (promocode != null && promocode.getTitle() != null) {
      dataModel.put("promoTitle", promocode.getTitle());
    }

    final boolean campaignRide = campaign != null;
    dataModel.put("isCampaignRide", campaignRide);
    if (campaignRide) {
      dataModel.put("campaignName", campaign.getName());
      dataModel.put("campaignCoverage", formatMoneyAmount(campaign.getCappedAmount()));
      dataModel.put("campaignImage", campaign.getHeaderImage());
    }

    dataModel.put("stripeCreditCharge", formatMoneyAmount(riderPayment.getStripeCreditCharge()));

    if (CollectionUtils.isNotEmpty(farePaymentsForParticipants)) {
      dataModel.put("farePaymentsForParticipants", farePaymentsForParticipants);
    }
    dataModel.put("city", city);

    return dataModel;
  }

}
