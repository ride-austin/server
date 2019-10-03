package com.rideaustin.service.reports;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.DefaultDateValues;

public class ReportParametersDefaultValueProviderImplTest {

  private ReportParametersDefaultValueProviderImpl testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new ReportParametersDefaultValueProviderImpl();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFillDefaultValuesFailsOnNonExistentMacroValue() throws Exception {
    Map<String, ReportParameter> mapping = createMapping(
      createParameter("START_MONTZ", ReportParameterType.DATE),
      "dateField"
    );

    testedInstance.fillDefaultValues(mapping, new Container());
  }

  @Test
  public void testFillDefaultValuesFillsMacroValue() throws Exception {
    Map<String, ReportParameter> mapping = createMapping(
      createParameter(DefaultDateValues.CURRENT_DATE.name(), ReportParameterType.DATE),
      "dateField"
    );
    Container container = new Container();

    testedInstance.fillDefaultValues(mapping, container);

    assertEquals(LocalDate.now().atStartOfDay().atZone(Constants.CST_ZONE).toInstant(), container.dateField);
  }

  @Test
  public void testFillDefaultValuesFillsInteger() throws Exception {
    Integer expected = 10;
    Map<String, ReportParameter> mapping = createMapping(
      createParameter(String.valueOf(expected), ReportParameterType.INTEGER),
      "intField"
    );
    Container container = new Container();

    testedInstance.fillDefaultValues(mapping, container);

    assertEquals(expected, container.intField);
  }

  @Test
  public void testFillDefaultValuesFillDouble() throws Exception {
    Double expected = 10.0;
    Map<String, ReportParameter> mapping = createMapping(
      createParameter(String.valueOf(expected), ReportParameterType.DECIMAL),
      "doubleField"
    );
    Container container = new Container();

    testedInstance.fillDefaultValues(mapping, container);

    assertEquals(expected, container.doubleField);
  }

  @Test
  public void testFillDefaultValuesFillString() throws Exception {
    String expected = "abc";
    Map<String, ReportParameter> mapping = createMapping(
      createParameter(expected, ReportParameterType.STRING),
      "stringField"
    );
    Container container = new Container();

    testedInstance.fillDefaultValues(mapping, container);

    assertEquals(expected, container.stringField);
  }

  private Map<String, ReportParameter> createMapping(ReportParameter parameter, String field) {
    return ImmutableMap.of(field, parameter);
  }

  private ReportParameter createParameter(String defaultValue, ReportParameterType parameterType) {
    ReportParameter parameter = new ReportParameter("", "", "", parameterType, false, false, defaultValue, 0, null);
    return parameter;
  }

  public class Container {
    private String stringField;
    private Integer intField;
    private Double doubleField;
    private Instant dateField;
    private Instant dateTimeField;

    public String getStringField() {
      return stringField;
    }

    public Integer getIntField() {
      return intField;
    }

    public Double getDoubleField() {
      return doubleField;
    }

    public Instant getDateField() {
      return dateField;
    }

    public Instant getDateTimeField() {
      return dateTimeField;
    }

    public void setStringField(String stringField) {
      this.stringField = stringField;
    }

    public void setIntField(Integer intField) {
      this.intField = intField;
    }

    public void setDoubleField(Double doubleField) {
      this.doubleField = doubleField;
    }

    public void setDateField(Instant dateField) {
      this.dateField = dateField;
    }

    public void setDateTimeField(Instant dateTimeField) {
      this.dateTimeField = dateTimeField;
    }
  }
}