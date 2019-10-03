package com.rideaustin.config;

import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SchedulerConfig {

  private final ApplicationContext context;
  private final DataSource dataSource;
  private final Environment env;

  @Bean
  @Profile("!itest")
  public SchedulerFactoryBean scheduler() {
    SchedulerFactoryBean sfb = new SchedulerFactoryBean();
    sfb.setDataSource(dataSource);
    sfb.setWaitForJobsToCompleteOnShutdown(true);
    sfb.setJobFactory(new AutowiringSpringBeanJobFactory(context.getAutowireCapableBeanFactory()));
    sfb.setAutoStartup(env.getProperty("quartz.auto_startup", Boolean.class, Boolean.TRUE));
    // Use a generate UUID for instance id
    UUID uuid = UUID.randomUUID();
    log.info("Starting quartz scheduler with instance id " + uuid);

    Properties qzProps = new Properties();
    qzProps.setProperty("org.quartz.scheduler.instanceId", uuid.toString());
    qzProps.setProperty("org.quartz.scheduler.idleWaitTime", env.getProperty("dispatch.quartz.idleWaitTime", "2000"));
    qzProps.setProperty("org.quartz.jobStore.isClustered", "true");
    qzProps.setProperty("org.quartz.threadPool.threadCount", env.getProperty("org.quartz.threadPool.threadCount", "20"));
    sfb.setQuartzProperties(qzProps);

    return sfb;
  }

  @Bean
  @Profile("itest")
  public SchedulerFactoryBean itestScheduler() {
    return new ITestSchedulerFactoryBean();
  }

  private static class ITestSchedulerFactoryBean extends SchedulerFactoryBean {
    @Override
    public void afterPropertiesSet() {
      //do nothing
    }
  }

  private static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
    private AutowireCapableBeanFactory beanFactory;

    private AutowiringSpringBeanJobFactory(AutowireCapableBeanFactory beanFactory) {
      this.beanFactory = beanFactory;
    }

    @Override
    public Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
      Object job = super.createJobInstance(bundle);
      beanFactory.autowireBean(job);
      return job;
    }
  }
}
