package com.rideaustin.utils;

import org.springframework.core.env.Environment;

public class PayoneerURLBuilder {
  private static final String GET_TOKEN = "GetToken";

  private static final String GET_PAYEE_DETAILS_URL = "GetPayeeDetails";

  private final String userName;

  private final String password;

  private final String partnerId;

  private final String baseUrl;

  private final String webAppURL;

  public PayoneerURLBuilder(Environment env) {
    userName = env.getProperty("payoneer.username");
    password = env.getProperty("payoneer.password");
    partnerId = env.getProperty("payoneer.partnerId");
    baseUrl = env.getProperty("payoneer.baseUrl");
    webAppURL = env.getProperty("webapp.url");
  }

  public String createGetTokenURL(String accountId) {
    StringBuilder url = createBaseURL(GET_TOKEN, accountId);
    addParam("p5", accountId, url);
    addParam("p6", webAppURL + "/x/payoneer.html", url);
    addParam("p8", "10", url);
    return url.toString();
  }

  public String createGetPayeeDetailsURL(String accountId) {
    return createBaseURL(GET_PAYEE_DETAILS_URL, accountId).toString();
  }

  private StringBuilder createBaseURL(String action, String accountId) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(baseUrl)
      .append("?mname=").append(action);
    addParam("p1", userName, stringBuilder);
    addParam("p2", password, stringBuilder);
    addParam("p3", partnerId, stringBuilder);
    addParam("p4", accountId, stringBuilder);
    return stringBuilder;
  }

  private void addParam(String name, String value, StringBuilder builder) {
    builder.append("&").append(name).append("=").append(value);
  }
}
