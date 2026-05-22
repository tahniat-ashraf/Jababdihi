package com.jababdihi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private final ApiKeyFilter apiKeyFilter;

  public SecurityConfig(ApiKeyFilter apiKeyFilter) {
    this.apiKeyFilter = apiKeyFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .permitAll()
                    .requestMatchers("/internal/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .build();
  }
}
