package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Test;

import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.utils.DateUtils;

public class SplitFareDtoAssemblerTest {

  private SplitFareDtoAssembler testedInstance = new SplitFareDtoAssembler();

  @Test
  public void toDtoFillsData() {
    FarePayment source = new FarePayment();
    final Ride ride = new Ride();
    final Rider rider = new Rider();
    final User user = new User();
    user.setFirstname("A");
    user.setLastname("B");
    user.setPhotoUrl("url");
    ride.setId(1L);
    rider.setId(1L);
    rider.setUser(user);
    source.setId(1L);
    source.setRide(ride);
    source.setRider(rider);
    source.setSplitStatus(SplitFareStatus.ACCEPTED);
    source.setCreatedDate(DateUtils.localDateToDate(LocalDate.of(2019, 12, 31), ZoneId.of("UTC")));
    source.setUpdatedDate(DateUtils.localDateToDate(LocalDate.of(2019, 12, 31), ZoneId.of("UTC")));

    final SplitFareDto result = testedInstance.toDto(source);

    assertEquals(source.getId(), result.getId());
    assertEquals(source.getRide().getId(), result.getRideId());
    assertEquals(source.getRider().getId(), result.getRiderId());
    assertEquals(source.getRider().getFullName(), result.getRiderFullName());
    assertEquals(source.getRider().getUser().getPhotoUrl(), result.getRiderPhoto());
    assertEquals(source.getSplitStatus(), result.getStatus());
    assertEquals("2019-12-31", result.getCreatedDate());
    assertEquals("2019-12-31", result.getUpdatedDate());
  }
}