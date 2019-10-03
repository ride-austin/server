package com.rideaustin.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.core.jmx.JobDataMapSupport;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.JobDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!itest")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SchedulerService {

  private SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss z");

  private final Scheduler scheduler;

  public void triggerJob(Class<? extends Job> jobClass, String name, String group, int delaySeconds,
    Map<String, Object> dataMap) throws ServerError {

    JobKey jobKey = JobKey.jobKey(name, group);
    JobDetail jobDetail = JobBuilder.newJob(jobClass)
      .withIdentity(jobKey)
      .usingJobData(JobDataMapSupport.newJobDataMap(dataMap))
      .build();
    Trigger trigger = TriggerBuilder.newTrigger()
      .startAt(DateBuilder.futureDate(delaySeconds, DateBuilder.IntervalUnit.SECOND))
      .build();
    try {
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  public boolean checkIfExists(String name, String group) throws ServerError {
    try {
      JobKey jobKey = JobKey.jobKey(name, group);
      return scheduler.checkExists(jobKey);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  public void removeJob(String name, String group) throws ServerError {
    try {
      JobKey jobKey = JobKey.jobKey(name, group);
      scheduler.deleteJob(jobKey);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  public void triggerJob(Class<? extends Job> jobClass, String name, String group, Map<String, Object> dataMap)
    throws ServerError {
    triggerJob(jobClass, name, group, 0, dataMap);
  }

  public void triggerJob(Class<? extends Job> jobClass, String name, int delaySeconds, Map<String, Object> dataMap)
    throws ServerError {
    triggerJob(jobClass, name, null, delaySeconds, dataMap);
  }

  public void triggerJob(Class<? extends Job> jobClass, Map<String, Object> dataMap) throws SchedulerException {
    JobKey jobKey = JobKey.jobKey(jobClass.getSimpleName());
    scheduler.addJob(JobBuilder.newJob(jobClass).withIdentity(jobKey).storeDurably().build(), true);
    scheduler.triggerJob(jobKey, JobDataMapSupport.newJobDataMap(dataMap));
  }

  public void triggerJob(String name, String group) throws ServerError {
    try {
      scheduler.triggerJob(new JobKey(name, group));
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  public void pauseJob(String name, String group) throws ServerError {
    try {
      JobKey jobKey = JobKey.jobKey(name, group);
      scheduler.pauseJob(jobKey);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  public void resumeJob(String name, String group) throws ServerError {
    try {
      JobKey jobKey = JobKey.jobKey(name, group);
      scheduler.resumeJob(jobKey);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  public Map<String, List<JobDto>> listCurrentJobs() throws SchedulerException {
    Map<String, List<JobDto>> jobs = new HashMap<>();

    scheduler.getJobGroupNames().forEach(groupName -> {
      List<JobDto> groupList = new ArrayList<>();
      try {
        scheduler.getJobKeys(GroupMatcher.groupEquals(groupName)).forEach(jobKey -> groupList.addAll(fillGroupList(jobKey)));
      } catch (Exception e) {
        log.error("Loading quartz group error. {}", e.getMessage(), e);
      }
      jobs.put(groupName, groupList);
    });
    return jobs;
  }

  private List<JobDto> fillGroupList(JobKey jobKey) {
    List<JobDto> groupList = new LinkedList<>();
    try {
      List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
      if (CollectionUtils.isNotEmpty(triggers)) {
        Trigger trigger = triggers.get(0);
        Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
        groupList.add(new JobDto(jobKey.getName(), defaultDateFormat.format(trigger.getNextFireTime()), state));
      }
    } catch (Exception e) {
      log.error("Loading quartz jobs error. {}", e.getMessage(), e);
    }
    return groupList;
  }

  public void scheduleJob(@Nonnull Class<? extends QuartzJobBean> clazz, String cronExpression) throws SchedulerException {
    scheduleJob(clazz, cronExpression, new HashMap<>());
  }

  public void scheduleJob(@Nonnull Class<? extends QuartzJobBean> clazz, String cronExpression, Map<String, Object> dataMap) throws SchedulerException {
    JobKey jobKey = toJobKey(clazz);
    if (cronExpression == null) {
      log.warn("no cron expression for the job {}", jobKey);
      return;
    }

    TriggerKey triggerKey = toTriggerKey(clazz);

    JobDetail jobDetail = JobBuilder.newJob(clazz)
      .withIdentity(jobKey)
      .usingJobData(JobDataMapSupport.newJobDataMap(dataMap))
      .build();

    if (!scheduler.checkExists(jobKey)) {

      log.info("scheduling job: {}, cron expression: {}", jobKey, cronExpression);

      CronTrigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey)
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
        .startNow()
        .build();

      scheduler.scheduleJob(jobDetail, trigger);

    } else if (scheduler.checkExists(triggerKey)) {
      CronTrigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey)
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
        .startNow()
        .build();
      scheduler.rescheduleJob(triggerKey, trigger);
    }
  }

  private static JobKey toJobKey(Class<? extends QuartzJobBean> clazz) {
    return JobKey.jobKey(clazz.getSimpleName());
  }

  private static TriggerKey toTriggerKey(Class<? extends QuartzJobBean> clazz) {
    return TriggerKey.triggerKey("Trigger." + clazz.getSimpleName());
  }

}
