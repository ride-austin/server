package com.rideaustin.service.reports;

import com.rideaustin.report.Report;
import com.rideaustin.report.render.ReportRenderer;
import com.rideaustin.rest.exception.RideAustinException;

@FunctionalInterface
public interface ReportExecutionService {

  <T, P> ReportRenderer execute(Report<T, P> report) throws RideAustinException;

}
