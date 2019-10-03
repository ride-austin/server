package com.rideaustin.test.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.CurrentUserService;

public class TestUtils {

  public static final Random RANDOM = new Random();

  public static final String REGULAR = "REGULAR";
  public static final String SUV = "SUV";
  public static final String PREMIUM = "PREMIUM";
  public static final String HONDA = "HONDA";
  public static final String LUXURY = "LUXURY";

  public static <T> T unwrapProxy(T object) throws Exception {
    if (AopUtils.isAopProxy(object) && object instanceof Advised) {
      return (T) ((Advised) object).getTargetSource().getTarget();
    } else {
      return object;
    }
  }

  public static User setupCurrentUser(CurrentUserService currentUserServiceMock, AvatarType avatarType) {
    User currentUser = new User();
    currentUser.setId(RANDOM.nextLong());
    currentUser.setEmail(Math.abs(RANDOM.nextLong()) + "@example.com");
    Set<AvatarType> avatarTypes = new HashSet<>();
    avatarTypes.add(avatarType);
    ReflectionTestUtils.setField(currentUser, "avatarTypes", avatarTypes);

    Mockito.when(currentUserServiceMock.getUser()).thenReturn(currentUser);
    return currentUser;
  }

  public static User setupCurrentUser(Object controller, AvatarType avatarType) throws Exception {
    CurrentUserService currentUserService = Mockito.mock(CurrentUserService.class);
    User currentUser = setupCurrentUser(currentUserService, avatarType);

    ReflectionTestUtils.setField(unwrapProxy(controller), "cuSvc", currentUserService);

    UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
    Mockito.when(userDetailsService.loadUserByUsername(Matchers.any(String.class))).thenReturn(currentUser);

    return currentUser;
  }

  public static <T> T getResponseData(MockMvc mockMvc, ObjectMapper objectMapper, String url, Class<T> dataClass)
    throws Exception {
    return objectMapper.readValue(getResult(mockMvc, get(url)), dataClass);
  }

  public static <T> T getResponseData(MockMvc mockMvc, ObjectMapper objectMapper, String url, TypeReference<T> dataClass)
    throws Exception {
    return objectMapper.readValue(getResult(mockMvc, get(url)), dataClass);
  }

  public static <T> T getResponseData(MockMvc mockMvc, ObjectMapper objectMapper,
    RequestBuilder request, Class<T> dataClass) throws Exception {
    return objectMapper.readValue(getResult(mockMvc, request), dataClass);
  }

  public static <T> T getResponseData(MockMvc mockMvc, ObjectMapper objectMapper, RequestBuilder request,
    TypeReference<T> typeRef) throws Exception {
    return objectMapper.readValue(getResult(mockMvc, request), typeRef);
  }

  public static <T> List<T> getResponsePage(MockMvc mockMvc, ObjectMapper objectMapper, String url,
    TypeReference<PageBean<T>> typeRef) throws Exception {
    return getResponseData(mockMvc, objectMapper, url, typeRef).getContent();
  }

  public static <T> List<T> getResponsePage(MockMvc mockMvc, ObjectMapper objectMapper, RequestBuilder request,
    TypeReference<PageBean<T>> typeRef) throws Exception {
    return getResponseData(mockMvc, objectMapper, request, typeRef).getContent();
  }

  private static String getResult(MockMvc mockMvc, RequestBuilder request) throws Exception {
    return mockMvc.perform(request)
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  public static Money money(double amount) {
    return Money.of(CurrencyUnit.USD, BigDecimal.valueOf(amount));
  }

  public static Money moneyOrNull(Double amount) {
    return Optional.ofNullable(amount).map(TestUtils::money).orElse(null);
  }

  public static HttpHeaders authorization(String login) {
    return authorization(login, login);
  }

  private static HttpHeaders authorization(String login, String password) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", Base64.getEncoder().encodeToString((String.format("%s:%s", login, login)).getBytes())));
    return httpHeaders;
  }

  public static RideEndLocation mockEndLocation() {
    RideEndLocation endLocation = new RideEndLocation();
    endLocation.setEndAddress("End address");
    endLocation.setEndZipCode("12345");
    endLocation.setEndLocationLat(10.0d);
    endLocation.setEndLocationLong(10.0d);
    return endLocation;
  }

  public static CarType mockCarType() {
    CarType carType = new CarType();
    carType.setCarCategory(TestUtils.REGULAR);
    carType.setBitmask(1);
    return carType;
  }

  public static boolean locationEquals(LatLng a, LatLng b) {
    if (a == null && b == null) {
      return true;
    } else if (a == null || b == null) {
      return false;
    } else {
      return Double.compare(a.lat, b.lat) == 0 && Double.compare(a.lng, b.lng) == 0;
    }
  }

}
