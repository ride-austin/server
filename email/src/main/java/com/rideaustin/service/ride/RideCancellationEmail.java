package com.rideaustin.service.ride;

import static com.rideaustin.utils.FormatUtils.formatDate;
import static com.rideaustin.utils.FormatUtils.formatMoneyAmount;
import static com.rideaustin.utils.FormatUtils.formatTime;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.EmailException;
import org.joda.money.MoneyUtils;

import com.rideaustin.model.City;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.email.AbstractPaymentTemplateEmail;
import com.rideaustin.service.model.FarePaymentDto;

public class RideCancellationEmail extends AbstractPaymentTemplateEmail {

  private static final String TITLE = "Your ride was canceled";
  private static final String TEMPLATE = "rider_cancellation_receipt.ftl";

  public RideCancellationEmail(Ride ride, @Nonnull byte[] imageData, City city, FarePaymentDto riderPayment) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(createRideDataModel(ride, city, riderPayment));
    embed(new ByteArrayDataSource(imageData, "image/png"), "routeMap.png", "routemap");
    addRecipient(ride.getRider().getFullName(), ride.getRider().getEmail());
  }

  private Map<String, Object> createRideDataModel(@Nonnull Ride ride, City city, FarePaymentDto riderPayment) {
    Map<String, Object> dataModel = new HashMap<>();
    dataModel.put("rideDate", formatDate(ride.getCreatedDate()));
    dataModel.put("totalFare", formatMoneyAmount(ride.getTotalFare()));
    dataModel.put("startLocation", ride.getStart().getAddress());

    if (ride.getStartedOn() != null) {
      dataModel.put("startTime", formatTime(ride.getStartedOn()));
    } else {
      dataModel.put("startTime", formatTime(ride.getCreatedDate()));
    }

    insertPaymentMethodInformation(dataModel, riderPayment);

    // Check for free credit used
    dataModel.put("hasPromocode", MoneyUtils.isPositive(ride.getFreeCreditCharged()));
    dataModel.put("freeCreditCharge", formatMoneyAmount(ride.getFreeCreditCharged()));
    dataModel.put("stripeCreditCharge", formatMoneyAmount(ride.getStripeCreditCharge()));
    dataModel.put("city", city);

    return dataModel;
  }

}
