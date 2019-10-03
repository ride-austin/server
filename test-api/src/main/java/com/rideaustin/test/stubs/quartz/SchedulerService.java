package com.rideaustin.test.stubs.quartz;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.quartz.Job;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;

public class SchedulerService extends com.rideaustin.service.SchedulerService {

  private final ApplicationContext context;
  private Queue<ExecutionContext> jobQueue;
  private boolean exists;

  public SchedulerService(ApplicationContext context) {
    super(null);
    this.context = context;
    jobQueue = new LinkedList<>();
  }

  @Override
  public boolean checkIfExists(String name, String group) {
    return exists;
  }

  @Override
  public void triggerJob(Class<? extends Job> jobClass, String name, String group, int delaySeconds,
    Map<String, Object> dataMap) {
    if (delaySeconds == 0) {
      executeJob(jobClass, dataMap);
    } else {
      jobQueue.add(new ExecutionContext(jobClass, dataMap, name));
    }
  }

  @Override
  public void triggerJob(Class<? extends Job> jobClass, Map<String, Object> dataMap) {
    executeJob(jobClass, dataMap);
  }

  @Override
  public void triggerJob(String name, String group) {

  }

  @Override
  public void removeJob(String name, String group) {
    //
  }

  public void executeNext() {
    while(jobQueue.iterator().hasNext()) {
      ExecutionContext next = jobQueue.poll();
      executeJob(next.jobClass, next.dataMap);
    }
  }

  public void discardQueue() {
    jobQueue.clear();
    exists = false;
  }

  private void executeJob(final Class<? extends Job> jobClass, Map<String, Object> dataMap) {

    try {
      final Job quartzJob = jobClass.newInstance();
      BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(quartzJob);
      MutablePropertyValues pvs = new MutablePropertyValues();
      pvs.addPropertyValues(dataMap);
      bw.setPropertyValues(pvs, true);
      context.getAutowireCapableBeanFactory().autowireBean(quartzJob);

      //get method executeInternal(JobExecutionContext) from job class extending QuartzJobBean
      final Method executeJobMethod = quartzJob.getClass().getDeclaredMethod("executeInternal");
      executeJobMethod.setAccessible(true);
      //call the processItems method on the Job class instance
      executeJobMethod.invoke(quartzJob);
    } catch (final Exception e) {
      throw new RuntimeException(String.format("Exception while retrieving and executing job for name=%s", jobClass.getName()), e);
    }
  }

  private static class ExecutionContext {
    final Class<? extends Job> jobClass;
    final Map<String, Object> dataMap;
    final String name;

    private ExecutionContext(Class<? extends Job> jobClass, Map<String, Object> dataMap, String name) {
      this.jobClass = jobClass;
      this.dataMap = dataMap;
      this.name = name;
    }
  }

}
