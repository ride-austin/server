package com.rideaustin.test.actions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static com.rideaustin.test.util.TestUtils.authorization;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.Api;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;

public class UserAction extends AbstractAction {

  public UserAction(MockMvc mockMvc, ObjectMapper mapper) {
    super(mockMvc, mapper);
  }

  @Override
  protected String getAvatarType() {
    return AvatarType.NAME_API_CLIENT;
  }

  public ResultActions signUp(User user, Long cityId) throws Exception {
    return signUp(user.getEmail(), user.getFacebookId(), user.getFirstname(), user.getLastname(), user.getPassword(), user.getPhoneNumber(),
      null, user.isPhoneNumberVerified(), cityId);
  }

  public ResultActions signUp(String email, String socialId, String firstname, String lastname, String password,
    String phonenumber, String data, boolean phonenumberVerified, Long cityId) throws Exception {
    return mockMvc.perform(post(Api.USERS)
      .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
      .headers(authorization(email))
      .param("email", email)
      .param("socialId", socialId)
      .param("firstname", firstname)
      .param("lastname", lastname)
      .param("password", password)
      .param("phonenumber", phonenumber)
      .param("data", data)
      .param("phonenumberVerified", String.valueOf(phonenumberVerified))
      .param("cityId", String.valueOf(cityId)));
  }

  public ResultActions forgotPassword(String email) throws Exception {
    return mockMvc.perform(post(String.format("%s/%s", Api.BASE, "forgot"))
      .headers(authorization(email))
      .param("email", email));
  }

  public ResultActions requestPhoneVerificationCode(String email, String phoneNumber) throws Exception {
    return mockMvc.perform(post(String.format("%s/%s", Api.PHONE_VERIFICATION, "requestCode"))
      .headers(authorization(email))
      .param("phoneNumber", phoneNumber));
  }

  public ResultActions userExists(String email, String phoneNumber) throws Exception {
    return mockMvc.perform(post(String.format("%s/%s", Api.USERS, "exists"))
      .headers(authorization(email))
      .param("email", email)
      .param("phoneNumber", phoneNumber));
  }
}
