package com.rideaustin.service.reports;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.AirportRidesReportParams;
import com.rideaustin.rest.exception.BadRequestException;

public class ReportParametersValidatorImplTest {

  private ReportParametersValidatorImpl testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new ReportParametersValidatorImpl();
  }

  @Test(expected = BadRequestException.class)
  public void testValidateThrowsExceptionOnNullRequiredValue() throws Exception {
    Map<String, ReportParameter> mapping = ImmutableMap.of(
      "startDate", createParameter("startDate")
    );
    testedInstance.validate(mapping, new AirportRidesReportParams());
  }

  @Test(expected = BadRequestException.class)
  public void testValidateThrowsExceptionOnEmptyStringRequiredValue() throws Exception {
    Field field = Container.class.getDeclaredField("field");
    field.setAccessible(true);
    Map<String, ReportParameter> mapping = ImmutableMap.of(
      "field", createParameter("field")
    );
    testedInstance.validate(mapping, new Container());
  }

  private ReportParameter createParameter(String name) {
    return new ReportParameter("", name, "", ReportParameterType.STRING, true, false, null, 0, null);
  }

  public class Container {
    private String field = "";

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }
  }
}