package com.rideaustin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rideaustin.report.render.BaseCompositeReportRenderer;
import com.rideaustin.report.render.CSVReportRenderer;
import com.rideaustin.report.render.XLSXReportRenderer;
import com.rideaustin.service.thirdparty.S3StorageService;

@Configuration
public class ReportsConfiguration {

  @Bean
  public BaseCompositeReportRenderer csvCompositeRenderer(S3StorageService s3StorageService) {
    return new BaseCompositeReportRenderer(new CSVReportRenderer(s3StorageService), s3StorageService);
  }

  @Bean
  public BaseCompositeReportRenderer xlsxCompositeRenderer(S3StorageService s3StorageService) {
    return new BaseCompositeReportRenderer(new XLSXReportRenderer(s3StorageService), s3StorageService);
  }
}
