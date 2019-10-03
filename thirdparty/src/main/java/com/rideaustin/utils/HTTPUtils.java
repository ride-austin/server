package com.rideaustin.utils;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPUtils {

  private HTTPUtils(){}

  public static String callURL(String uri) throws RideAustinException {
    HttpGet httpGet = new HttpGet(uri);
    try (CloseableHttpResponse response = HttpClientBuilder.create().build().execute(httpGet)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        throw new ServerError("Could not finish operation. Bad status code "+statusCode);
      }
      return EntityUtils.toString(response.getEntity());
    } catch (IOException e) {
      log.error("IO Exception, " + uri, e);
      throw new ServerError("IO Exception during call.", e);
    }
  }

}
