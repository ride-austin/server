package com.rideaustin.service.thirdparty;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.utils.HTTPUtils;
import com.rideaustin.utils.PayoneerURLBuilder;
import com.rideaustin.utils.XMLUtils;

@Service
@Profile("!itest")
public class PayoneerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PayoneerService.class);
  private static final String SUCCESS = "000";
  private static final String PAYEE_NOT_EXIST = "002";

  private final Environment env;

  @Inject
  public PayoneerService(Environment env) {
    this.env = env;
  }

  public String getSignupURL(String payoneerId) throws RideAustinException {
    String uri = new PayoneerURLBuilder(env).createGetTokenURL(payoneerId);
    LOGGER.info("Calling: {}", uri);
    String retURL = retrievePayoneerData(uri);
    LOGGER.info("Received: {}", retURL);
    try {
      return URLDecoder.decode(retURL, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Error while creating payoneer signup URL " + retURL, e);
      throw new ServerError("Error while creating payoneer signup URL");
    }
  }

  public PayoneerStatus getPayoneerStatus(String payoneerId) throws RideAustinException {
    if (StringUtils.isEmpty(payoneerId)) {
      throw new BadRequestException("Invalid payoneer id");
    }
    String uri = new PayoneerURLBuilder(env).createGetPayeeDetailsURL(payoneerId);
    LOGGER.info("Calling: {}", uri);
    String retData = retrievePayoneerData(uri);
    LOGGER.info("Received: {}", retData);
    String code = Optional.ofNullable(XMLUtils.getXMLNode(retData, "/GetPayeeDetails/Code")).map(String::trim).orElse(SUCCESS);
    if (SUCCESS.equals(code)) {
      return PayoneerStatus.from(XMLUtils.getXMLNode(retData, "/GetPayeeDetails/Payee/PayeeStatus"));
    } else if (PAYEE_NOT_EXIST.equals(code)) {
      return PayoneerStatus.INITIAL;
    }
    throw new ServerError("Unknown Payoneer error: " + XMLUtils.getXMLNode(retData, "/GetPayeeDetails/Error"));
  }

  protected String retrievePayoneerData(String uri) throws RideAustinException {
    return HTTPUtils.callURL(uri);
  }
}
