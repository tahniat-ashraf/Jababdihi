package com.jababdihi.backend.config;

import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {
  private final String key;

  public ApiKeyAuthentication(String key) {
    super(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    this.key = key;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return key;
  }

  @Override
  public Object getPrincipal() {
    return "api-key";
  }
}
