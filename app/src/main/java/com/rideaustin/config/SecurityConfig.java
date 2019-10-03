package com.rideaustin.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

import com.rideaustin.security.BasicAndTokenAuthenticationFilter;
import com.rideaustin.security.CustomBasicAuthenticationEntryPoint;
import com.rideaustin.security.SimpleCORSFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final Environment env;
  private final BasicAndTokenAuthenticationFilter authFilter;
  private final AuthenticationProvider authenticationProvider;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    HttpSecurity httpSecurity = initializeSecurity(http);
    httpSecurity = addRouteSecurity(httpSecurity);
    finalizeSettings(httpSecurity);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  private HttpSecurity initializeSecurity(HttpSecurity http) {
    return http.addFilterBefore(new SimpleCORSFilter(), ChannelProcessingFilter.class);
  }

  private HttpSecurity addRouteSecurity(HttpSecurity httpSecurity) throws Exception {
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
      httpSecurity.authorizeRequests();
    if (env.getProperty("swagger.enable", Boolean.class, false)) {
      registry = registry.antMatchers("/swagger-*/**", "/v2/api-docs", "/webjars/**").permitAll();
    }
    return registry
      .antMatchers(HttpMethod.POST, "/rest/riders").permitAll()
      .antMatchers("/rest/users/current").authenticated()
      .antMatchers("/rest/users/**").permitAll()
      .antMatchers(HttpMethod.POST, "/rest/facebook").permitAll()
      .antMatchers(HttpMethod.GET, "/password-reset").permitAll()
      .antMatchers(HttpMethod.POST, "/rest/forgot").permitAll()
      .antMatchers(HttpMethod.GET, "/rest/health").permitAll()
      .antMatchers(HttpMethod.HEAD, "/rest/health").permitAll()
      .antMatchers(HttpMethod.GET, "/rest/rides/*/allTrackers").permitAll()
      .antMatchers(HttpMethod.POST, "/rest/sms").permitAll()
      .antMatchers(HttpMethod.POST, "/rest/sms/callback").permitAll()
      .antMatchers(HttpMethod.POST, "/rest/phoneVerification/*").permitAll()
      .antMatchers(HttpMethod.GET, "/rest/configs/app/info/current*").permitAll()
      .antMatchers(HttpMethod.POST, "/rest/bounce").permitAll()
      .antMatchers(HttpMethod.GET, "/emailVerification").permitAll()
      .antMatchers(HttpMethod.GET, "/fonts/*").permitAll()
      .anyRequest().authenticated()
      .and();
  }

  private void finalizeSettings(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.securityContext().securityContextRepository(new NullSecurityContextRepository()).and()
      .csrf().disable()
      .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .exceptionHandling().authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint(env));

  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authenticationProvider);
  }

}