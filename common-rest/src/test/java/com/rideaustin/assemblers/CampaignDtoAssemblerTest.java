package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.enums.CampaignAreaType;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.rest.model.CampaignDto;

public class CampaignDtoAssemblerTest {

  private CampaignDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new CampaignDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final CampaignDto result = testedInstance.toDto((Campaign) null);

    assertNull(result);
  }

  @Test
  public void toDto() {
    final String name = "Campaign";
    final String headerIcon = "header";
    final String descriptionBody = "Description";
    final String footerText = "Footer";
    final Campaign source = new Campaign();
    source.setName(name);
    source.setHeaderIcon(headerIcon);
    source.setDescriptionBody(descriptionBody);
    source.setFooterText(footerText);

    final String pickupAreaName = "Pickup area";
    List<Pair<Double, Double>> points = ImmutableList.of(
      ImmutablePair.of(-97.4651,34.456),
      ImmutablePair.of(-97.4951,34.6498),
      ImmutablePair.of(-97.91681,34.948165)
    );
    final String csvGeometry = points.stream().map(p -> String.format("%f,%f", p.getLeft(), p.getRight())).collect(Collectors.joining(" "));
    final long pickupAreaId = 1L;
    final CampaignArea pickupZone = createArea(pickupAreaName, csvGeometry, pickupAreaId);
    source.setPickupZones(Collections.singleton(pickupZone));

    final String dropoffAreaName = "Dropoff area";
    final long dropoffAreaId = 2L;
    final CampaignArea dropoffZone = createArea(dropoffAreaName, csvGeometry, dropoffAreaId);
    source.setDropoffZones(Collections.singleton(dropoffZone));

    final CampaignDto result = testedInstance.toDto(source);

    assertEquals(name, result.getHeaderTitle());
    assertEquals(headerIcon, result.getHeaderIcon());
    assertEquals(descriptionBody, result.getBody());
    assertEquals(footerText, result.getFooter());
  }

  private CampaignArea createArea(final String name, final String csvGeometry, final long id) {
    final CampaignArea pickupZone = new CampaignArea();
    final AreaGeometry pickupArea = new AreaGeometry();
    pickupArea.setId(id);
    pickupArea.setName(name);
    pickupArea.setCsvGeometry(csvGeometry);
    pickupZone.setArea(pickupArea);
    pickupZone.setSubType(CampaignAreaType.SubType.BUS_STOP);
    return pickupZone;
  }
}