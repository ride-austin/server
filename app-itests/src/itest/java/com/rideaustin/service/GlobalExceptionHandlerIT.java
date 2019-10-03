package com.rideaustin.service;

import static com.rideaustin.test.util.TestUtils.unwrapProxy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.service.email.EmailService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, GlobalExceptionHandlerTestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class GlobalExceptionHandlerIT {


  private MockMvc mockMvc;

  @Inject
  private WebApplicationContext webApplicationContext;

  @Inject
  private GlobalExceptionEmailHelper globalExceptionEmailHelper;

  @Mock
  private EmailService emailService;

  private CountDownLatch latch = new CountDownLatch(1);

  private Object someValueForAssert;

  @Before
  public void before() throws Exception {
    mockMvc = webAppContextSetup(webApplicationContext).build();
    MockitoAnnotations.initMocks(this);


    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        latch.countDown();
        someValueForAssert = "email was sent";
        return someValueForAssert;
      }
    }).when(emailService).sendEmail(any(AbstractTemplateEmail.class));


    GlobalExceptionEmailHelper unwrappedExceptionEmailHelper = unwrapProxy(this.globalExceptionEmailHelper);
    ReflectionTestUtils.setField(unwrappedExceptionEmailHelper, "emailService", emailService);

  }

  @Test
  public void test() throws Exception {
    mockMvc.perform(get("/rest/test-exception/"))
      .andDo(print())
      .andExpect(status().is5xxServerError())
      .andExpect(jsonPath("$").value(notNullValue()))
      .andExpect(jsonPath("$").value(containsString("error")))
    ;

    latch.await(10, TimeUnit.SECONDS);

    assertThat(someValueForAssert).isNotNull();
  }


}