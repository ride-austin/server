package com.rideaustin.report.entry;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.util.Map;

import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TNCCompositeReport;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TNCSummaryReportEntry implements TupleConsumer {

  private static final String SURGE_PRICE_EFFECT_HEADER = "Amount of time that surge pricing was in effect ";

  @ReportField(order = 1)
  private Long tripsCompleted;
  @ReportField(order = 2)
  private Long passengersTransported;
  @ReportField(order = 3)
  private BigDecimal grossReceiptsGenerated;
  @ReportField(order = 4)
  private BigDecimal hoursDrivenByCompliantDrivers;
  @ReportField(order = 5)
  private BigDecimal hoursDrivenByAllDrivers;
  @ReportField(order = 6)
  private BigDecimal milesDrivenByCompliantDrivers;
  @ReportField(order = 7)
  private BigDecimal milesDrivenByAllDrivers;
  @ReportField(order = 8, name = "Number of vehicles logged into the TNC platform")
  private Long vehiclesLoggedIntoPlatform;

  private Map<String, BigDecimal> hoursSurgePricingInEffect;

  public TNCSummaryReportEntry(Tuple tuple) {
    int index = 0;
    this.tripsCompleted = getLong(tuple, index++);
    this.passengersTransported = getLong(tuple, index++);
    this.grossReceiptsGenerated = getBigDecimal(tuple, index++);
    this.hoursDrivenByCompliantDrivers = BigDecimal.ZERO;
    this.hoursDrivenByAllDrivers = getBigDecimal(tuple, index++).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE);
    this.milesDrivenByCompliantDrivers = BigDecimal.ZERO;
    this.milesDrivenByAllDrivers = getBigDecimal(tuple, index).multiply(Constants.MILES_PER_METER);
  }

  public void setVehiclesLoggedIntoPlatform(Long vehiclesLoggedIntoPlatform) {
    this.vehiclesLoggedIntoPlatform = vehiclesLoggedIntoPlatform;
  }

  public void setHoursSurgePricingInEffect(Map<String, BigDecimal> hoursSurgePricingInEffect) {
    this.hoursSurgePricingInEffect = hoursSurgePricingInEffect;
  }

  @ReportField(order = 9, name = SURGE_PRICE_EFFECT_HEADER + TNCCompositeReport.TWO_AM_SIX_AM)
  public BigDecimal getSurgePricingEffect2am6am() {
    return safeZero(hoursSurgePricingInEffect.get(TNCCompositeReport.TWO_AM_SIX_AM));
  }

  @ReportField(order = 10, name = SURGE_PRICE_EFFECT_HEADER + TNCCompositeReport.SIX_AM_TEN_AM)
  public BigDecimal getSurgePricingEffect6am10am() {
    return safeZero(hoursSurgePricingInEffect.get(TNCCompositeReport.SIX_AM_TEN_AM));
  }

  @ReportField(order = 11, name = SURGE_PRICE_EFFECT_HEADER + TNCCompositeReport.TEN_AM_TWO_PM)
  public BigDecimal getSurgePricingEffect10am2pm() {
    return safeZero(hoursSurgePricingInEffect.get(TNCCompositeReport.TEN_AM_TWO_PM));
  }

  @ReportField(order = 12, name = SURGE_PRICE_EFFECT_HEADER + TNCCompositeReport.TWO_PM_SIX_PM)
  public BigDecimal getSurgePricingEffect2pm6pm() {
    return safeZero(hoursSurgePricingInEffect.get(TNCCompositeReport.TWO_PM_SIX_PM));
  }

  @ReportField(order = 13, name = SURGE_PRICE_EFFECT_HEADER + TNCCompositeReport.SIX_PM_TEN_PM)
  public BigDecimal getSurgePricingEffect6pm10pm() {
    return safeZero(hoursSurgePricingInEffect.get(TNCCompositeReport.SIX_PM_TEN_PM));
  }

  @ReportField(order = 14, name = SURGE_PRICE_EFFECT_HEADER + TNCCompositeReport.TEN_PM_TWO_AM)
  public BigDecimal getSurgePricingEffect10pm2am() {
    return safeZero(hoursSurgePricingInEffect.get(TNCCompositeReport.TEN_PM_TWO_AM));
  }

}
