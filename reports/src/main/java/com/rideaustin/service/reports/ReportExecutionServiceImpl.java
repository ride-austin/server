package com.rideaustin.service.reports;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.report.Report;
import com.rideaustin.report.render.ReportRenderer;
import com.rideaustin.report.render.ReportRendererFactory;
import com.rideaustin.rest.exception.RideAustinException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportExecutionServiceImpl implements ReportExecutionService {

  private final ReportRendererFactory representationFactory;

  @Override
  public ReportRenderer execute(Report report) throws RideAustinException {
    report.execute();
    return representationFactory.getRendererFor(report);
  }

}
