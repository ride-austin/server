package com.rideaustin.report.render;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.report.Report;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportRendererFactory {

  private final Set<ReportRenderer> renderers;

  public ReportRenderer getRendererFor(final Report report) throws RideAustinException {
    return renderers.stream()
      .filter(r -> r.canRepresent(report))
      .findFirst()
      .orElseThrow(() -> new ServerError("Unknown report format"));
  }
}
