package com.rideaustin.report.render;

import java.util.List;

import com.rideaustin.model.City;
import com.rideaustin.report.Report;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.email.ReportEmail;

public interface ReportRenderer<S extends Report> {
  ReportEmail createEmailFor(S report, List<String> recipient, City city) throws RideAustinException;
  boolean canRepresent(Report representation);
}
