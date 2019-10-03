package com.rideaustin.report.adapter;

import java.time.Instant;

import org.joda.money.Money;

import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;

public class Entry {
  @ReportField(order = 1)
  private String string;
  @ReportField(order = 2)
  private int integerField;
  @ReportField(order = 3)
  private double numeric;
  @ReportField(order = 4, name = "cash")
  private Money money;
  @ReportField(order = 5, format = FormatAs.DATE)
  private Instant date;
  @ReportField(order = 6, format = FormatAs.DATETIME)
  private Instant datetime;

  @ReportField(order = 7)
  public int getVirtual() {
    return integerField*5;
  }

  @ReportField(order = 8)
  public int getVirtualTen() {
    return integerField*10;
  }

  @ReportField(order = 9, name = "zero")
  public int getVirtualZero() {
    return 0;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public int getIntegerField() {
    return integerField;
  }

  public void setIntegerField(int integerField) {
    this.integerField = integerField;
  }

  public double getNumeric() {
    return numeric;
  }

  public void setNumeric(double numeric) {
    this.numeric = numeric;
  }

  public Money getMoney() {
    return money;
  }

  public void setMoney(Money money) {
    this.money = money;
  }

  public Instant getDate() {
    return date;
  }

  public void setDate(Instant date) {
    this.date = date;
  }

  public Instant getDatetime() {
    return datetime;
  }

  public void setDatetime(Instant datetime) {
    this.datetime = datetime;
  }
}