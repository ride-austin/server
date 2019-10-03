package com.rideaustin.service;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignRider;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideTrackerDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.service.strategy.CampaignEligibilityStrategy;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CampaignServiceTest {

    @Mock
    private ConfigurationItemCache configurationItemCache;
    @Mock
    private RideDslRepository rideDslRepository;
    @Mock
    private CampaignDslRepository repository;
    @Mock
    private RideTrackerDslRepository rideTrackerDslRepository;
    @Mock
    private RiderDslRepository riderDslRepository;
    @Mock
    private BeanFactory beanFactory;
    @Mock
    private CarTypesCache carTypesCache;
    @Mock
    private CampaignEligibilityStrategy eligibilityStrategy;

    private CampaignService testedInstance;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testedInstance = new CampaignService(configurationItemCache, rideDslRepository, repository, rideTrackerDslRepository,
            riderDslRepository, beanFactory);

        CarTypesUtils.setCarTypesCache(carTypesCache);
    }

    @Test
    public void findEligibleCampaignSkipsCampaignWithWrongWeekday() {
        final Campaign campaign = new Campaign();
        campaign.setActiveOnDays(0);
        when(repository.findAll()).thenReturn(ImmutableList.of(campaign));

        final Optional<Campaign> result = testedInstance.findEligibleCampaign(new Date(), new LatLng(34.186161, -97.186116),
            "REGULAR", new Rider());

        assertFalse(result.isPresent());
    }

    @Test
    public void findEligibleCampaignSkipsCampaignWithWrongRequestTime() {
        final Campaign campaign = new Campaign();
        campaign.setActiveOnDays(ConfigurationWeekday.fromWeekday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).getBitmask());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        campaign.setActiveFromHour(now.getHour() - 2);
        campaign.setActiveToHour(now.getHour() - 1);
        when(repository.findAll()).thenReturn(ImmutableList.of(campaign));

        final Optional<Campaign> result = testedInstance.findEligibleCampaign(new Date(), new LatLng(34.186161, -97.186116),
            "REGULAR", new Rider());

        assertFalse(result.isPresent());
    }

    @Test
    public void findEligibleCampaignSkipsCampaignWithWrongCarCategory() {
        final Campaign campaign = new Campaign();
        campaign.setActiveOnDays(ConfigurationWeekday.fromWeekday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).getBitmask());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        campaign.setActiveFromHour(now.getHour() - 1);
        campaign.setActiveToHour(now.getHour() + 1);
        campaign.setEligibleCarCategories(2);
        when(repository.findAll()).thenReturn(ImmutableList.of(campaign));
        when(carTypesCache.fromBitMask(2)).thenReturn(Collections.singleton("SUV"));

        final Optional<Campaign> result = testedInstance.findEligibleCampaign(new Date(), new LatLng(34.186161, -97.186116),
            "REGULAR", new Rider());

        assertFalse(result.isPresent());
    }

    @Test
    public void findEligibleCampaignSkipsCampaignWithWrongRider() {
        final Campaign campaign = new Campaign();
        campaign.setActiveOnDays(ConfigurationWeekday.fromWeekday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).getBitmask());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        campaign.setActiveFromHour(now.getHour() - 1);
        campaign.setActiveToHour(now.getHour() + 1);
        campaign.setEligibleCarCategories(1);
        campaign.setUserBound(true);
        final CampaignRider campaignRider = new CampaignRider();
        campaignRider.setEnabled(true);
        final Rider rider = new Rider();
        rider.setId(5L);
        campaignRider.setRider(rider);
        campaign.setSubscribedRiders(Collections.singleton(campaignRider));
        when(repository.findAll()).thenReturn(ImmutableList.of(campaign));
        when(carTypesCache.fromBitMask(1)).thenReturn(Collections.singleton("REGULAR"));

        final Optional<Campaign> result = testedInstance.findEligibleCampaign(new Date(), new LatLng(34.186161, -97.186116),
            "REGULAR", new Rider());

        assertFalse(result.isPresent());
    }

    @Test
    public void findEligibleCampaignSkipsCampaignWithWrongDistance() {
        final Campaign campaign = new Campaign();
        campaign.setActiveOnDays(ConfigurationWeekday.fromWeekday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).getBitmask());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        campaign.setActiveFromHour(now.getHour() - 1);
        campaign.setActiveToHour(now.getHour() + 1);
        campaign.setEligibleCarCategories(1);
        campaign.setUserBound(true);
        final CampaignRider campaignRider = new CampaignRider();
        campaignRider.setEnabled(true);
        final Rider rider = new Rider();
        campaignRider.setRider(rider);
        campaign.setSubscribedRiders(Collections.singleton(campaignRider));
        when(repository.findAll()).thenReturn(ImmutableList.of(campaign));
        when(carTypesCache.fromBitMask(1)).thenReturn(Collections.singleton("REGULAR"));
        campaign.setMaximumDistance(BigDecimal.valueOf(100L));

        final Optional<Campaign> result = testedInstance.findEligibleCampaign(new Date(), new LatLng(34.186161, -97.186116),
            new LatLng(34.19816, -97.186131), "REGULAR", rider, BigDecimal.valueOf(1000L));

        assertFalse(result.isPresent());
    }

    @Test
    public void findEligibleCampaignReturnsEligibleCampaign() {
        final Campaign campaign = new Campaign();
        campaign.setActiveOnDays(ConfigurationWeekday.fromWeekday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)).getBitmask());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        campaign.setActiveFromHour(now.getHour() - 1);
        campaign.setActiveToHour(now.getHour() + 1);
        campaign.setEligibleCarCategories(1);
        campaign.setUserBound(true);
        final CampaignRider campaignRider = new CampaignRider();
        campaignRider.setEnabled(true);
        final Rider rider = new Rider();
        campaignRider.setRider(rider);
        campaign.setSubscribedRiders(Collections.singleton(campaignRider));
        when(repository.findAll()).thenReturn(ImmutableList.of(campaign));
        when(carTypesCache.fromBitMask(1)).thenReturn(Collections.singleton("REGULAR"));
        campaign.setMaximumDistance(BigDecimal.valueOf(2000L));
        campaign.setEligibilityStrategy(CampaignEligibilityStrategy.class);
        when(beanFactory.getBean(CampaignEligibilityStrategy.class)).thenReturn(eligibilityStrategy);

        final LatLng startLocation = new LatLng(34.186161, -97.186116);
        final LatLng endLocation = new LatLng(34.19816, -97.186131);
        when(eligibilityStrategy.isEligible(startLocation, endLocation, rider, campaign)).thenReturn(true);

        final Optional<Campaign> result = testedInstance.findEligibleCampaign(new Date(), startLocation,
            endLocation, "REGULAR", rider, BigDecimal.valueOf(1000L));

        assertTrue(result.isPresent());
        assertEquals(campaign, result.get());
    }
}