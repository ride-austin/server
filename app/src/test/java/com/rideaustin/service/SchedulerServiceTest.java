package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;

import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.JobDto;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceTest {

  @Inject
  private SchedulerService schedulerService;

  @Mock
  private Scheduler scheduler;

  private static final String DEFAULT_JOB_NAME = "JOBNAME";

  private static final String DEFAULT_JOB_GROUP = "DEFAULT";

  private static final Date NEXT_FIRE_TIME = new Date();

  private SimpleDateFormat defaultDateFormat = new SimpleDateFormat("YYYY-mm-dd HH:mm:ss z");

  @Before
  public void setup() throws Exception {

    List<String> groupsNames = Collections.singletonList(DEFAULT_JOB_GROUP);
    Set<JobKey> jobKeys = new HashSet<>();
    jobKeys.add(new JobKey(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP));

    List triggers = new ArrayList<>();
    CronTriggerImpl ct = new CronTriggerImpl();
    ct.setNextFireTime(NEXT_FIRE_TIME);
    triggers.add(ct);

    when(scheduler.getJobGroupNames()).thenReturn(groupsNames);
    when(scheduler.getJobKeys(any())).thenReturn(jobKeys);
    when(scheduler.getTriggersOfJob(any())).thenReturn(triggers);
    when(scheduler.getTriggerState(any())).thenReturn(Trigger.TriggerState.NORMAL);

    schedulerService = new SchedulerService(scheduler);
  }

  @Test
  public void getListOfJobs() throws Exception {
    Map<String, List<JobDto>> jobs = schedulerService.listCurrentJobs();

    assertThat(jobs.containsKey(DEFAULT_JOB_GROUP), is(true));
    assertThat(jobs.get(DEFAULT_JOB_GROUP).size(), is(1));
    JobDto jobDto = jobs.get(DEFAULT_JOB_GROUP).get(0);
    assertThat(jobDto, notNullValue());
    assertThat(jobDto.getName(), is(DEFAULT_JOB_NAME));
    assertThat(jobDto.getNextFireTime(), is(defaultDateFormat.format(NEXT_FIRE_TIME)));
    assertThat(jobDto.getState(), is(Trigger.TriggerState.NORMAL));
  }

  @Test
  public void getListOfJobsNoTriggers() throws Exception {
    when(scheduler.getTriggersOfJob(any())).thenReturn(null);
    Map<String, List<JobDto>> jobs = schedulerService.listCurrentJobs();
    assertThat(jobs.containsKey(DEFAULT_JOB_GROUP), is(true));
    assertThat(jobs.get(DEFAULT_JOB_GROUP).size(), is(0));
  }

  @Test
  public void executeJob() throws Exception {
    schedulerService.triggerJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
  }

  @Test(expected = RuntimeException.class)
  public void executeJobException() throws Exception {
    doThrow(new RuntimeException()).when(scheduler).triggerJob(any());
    schedulerService.triggerJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
  }

  @Test
  public void shouldRemoveJob() {
    try {
      schedulerService.removeJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
    } catch (ServerError serverError) {
      fail();
    }
  }

  @Test(expected = ServerError.class)
  public void shouldTranslateSchedulerException_WhenRemovingJob() throws Exception {
    doThrow(new SchedulerException()).when(scheduler).deleteJob(any());

    schedulerService.removeJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
  }

  @Test
  public void shouldPauseJob() {
    try {
      schedulerService.pauseJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
    } catch (ServerError serverError) {
      fail();
    }
  }

  @Test(expected = ServerError.class)
  public void shouldTranslateSchedulerException_WhenPausingJob() throws Exception {
    doThrow(new SchedulerException()).when(scheduler).pauseJob(any());

    schedulerService.pauseJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
  }

  @Test
  public void shouldResumeJob() {
    try {
      schedulerService.resumeJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
    } catch (ServerError serverError) {
      fail();
    }
  }

  @Test(expected = ServerError.class)
  public void shouldTranslateSchedulerException_WhenResumingJob() throws Exception {
    doThrow(new SchedulerException()).when(scheduler).resumeJob(any());

    schedulerService.resumeJob(DEFAULT_JOB_NAME, DEFAULT_JOB_GROUP);
  }
}
