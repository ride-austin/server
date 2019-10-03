package com.rideaustin.config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePropertySource;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RAApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    addPropertiesFileToEnvironment(applicationContext);
  }

  private void addPropertiesFileToEnvironment(ConfigurableApplicationContext context) {
    ConfigurableEnvironment environment = context.getEnvironment();
    String activeProfile = "dev";
    if (environment.getActiveProfiles().length > 0) {
      activeProfile = environment.getActiveProfiles()[0];
    }

    log.info("Active profile is {}", activeProfile);

    String url = "classpath:/" + activeProfile + ".properties";
    String propertiesUrl = System.getProperty("ra.properties.url");
    if (propertiesUrl != null && !propertiesUrl.isEmpty()) {
      url = propertiesUrl;
    }

    log.info("Properties file url is {}", url);
    MapPropertySource propertiesSource = readFromPropertyURL(url);
    environment.getPropertySources().addLast(propertiesSource);
  }

  private MapPropertySource readFromPropertyURL(String url) {
    try {
      if (url.startsWith("classpath")) {
        return loadFromClassPath(url);
      } else if (url.startsWith("s3")) {
        return loadFromS3Resource(url);
      }
      return new ResourcePropertySource(new UrlResource(url));
    } catch (IOException e) {
      log.error("Couldn't load properties file from {}", url, e);
      return new MapPropertySource("null", new HashMap<>());
    }
  }

  private MapPropertySource loadFromS3Resource(String url) throws IOException {
    String awsKey = System.getProperty("ra.aws.key.access");
    String awsSecret = System.getProperty("ra.aws.key.secret");
    String awsBucket = System.getProperty("ra.s3.bucket");
    String s3FileKey = url.substring("s3:".length());
    AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(awsKey, awsSecret));
    File file = new File(System.getProperty("catalina.home") + "/app.properties");
    s3Client.getObject(new GetObjectRequest(awsBucket, s3FileKey), file);
    URI uri = file.toURI();
    log.info("Reading properties file at path {}", uri);
    return new ResourcePropertySource(new UrlResource(uri));
  }

  private MapPropertySource loadFromClassPath(String url) throws IOException {
    ClassPathResource resource = new ClassPathResource(url.substring("classpath:/".length()));
    log.info("Reading properties file at path {}", resource.getURL());
    return new ResourcePropertySource(resource);
  }
}
