package com.rideaustin.service.user;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.report.RidersExportReport;
import com.rideaustin.report.params.RidersExportReportParams;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.ListRidersParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.SimpleRiderDto;
import com.rideaustin.service.BaseAvatarService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SchedulerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RiderAdministrationService {

  private final RiderDslRepository riderDslRepository;

  private final SchedulerService schedulerService;
  private final ObjectMapper mapper;
  private final CurrentUserService currentUserService;
  private final BaseAvatarService baseAvatarService;

  public Page<Rider> listRiders(ListRidersParams params, PagingParams paging) {
    Page<Rider> riders = riderDslRepository.findRiders(params, paging);
    baseAvatarService.enrichAvatarWithLastLoginDate(riders);
    return riders;
  }

  public Page<SimpleRiderDto> listRidersToDto(ListRidersParams params, PagingParams paging) {
    return riderDslRepository.findRidersDto(params, paging);
  }

  public void exportRiders(ListRidersParams params) throws ServerError {
    Map<String, Object> data;
    try {
      data = ImmutableMap.of("recipients", Collections.singletonList(currentUserService.getUser().getEmail()),
        "paramsJson", mapper.writeValueAsString(new RidersExportReportParams(params)),
        "reportClass", RidersExportReport.class);
      schedulerService.triggerJob(GenericReportJob.class, data);
    } catch (JsonProcessingException | SchedulerException e) {
      throw new ServerError(e);
    }
  }
}
