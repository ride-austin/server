package com.rideaustin.report.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

import com.rideaustin.Constants;
import com.rideaustin.report.FormatAs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ReportParameterType {
  STRING {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return ReportParameter::getDefaultValue;
    }
  },
  INTEGER {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return p -> Integer.valueOf(p.getDefaultValue());
    }
  },
  DECIMAL {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return p -> Double.valueOf(p.getDefaultValue());
    }
  },
  DATE {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return p -> LocalDate.parse(p.getDefaultValue(), Constants.DATE_FORMATTER)
        .atStartOfDay().atZone(Constants.CST_ZONE).toInstant();
    }

    @Override
    public String applyFormat(Object value) {
      return FormatAs.DATE.toString(value);
    }
  },
  DATETIME {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return p -> LocalDateTime.parse(p.getDefaultValue()).atZone(Constants.CST_ZONE).toInstant();
    }

    @Override
    public String applyFormat(Object value) {
      return FormatAs.DATETIME.toString(value);
    }
  },
  BOOLEAN {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return p -> Boolean.valueOf(p.getDefaultValue());
    }
  },
  ENUM {
    @Override
    public Function<ReportParameter, Object> defaultValueConverter() {
      return p -> {
        try {
          Class clazz = Class.forName(p.getEnumClass());
          return Enum.valueOf(clazz, p.getDefaultValue());
        } catch (ClassNotFoundException e) {
          log.error("Failed to convert", e);
          return null;
        }
      };
    }
  };

  public abstract Function<ReportParameter, Object> defaultValueConverter();

  public String applyFormat(Object value) {
    return FormatAs.NONE.toString(value);
  }
}
