package edu.handong.csee.histudy.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.handong.csee.histudy.domain.Role;
import io.jsonwebtoken.Claims;

public final class AuthClaimsFactory {

  private AuthClaimsFactory() {}

  public static Claims userClaims(String email) {
    return claims(email, Role.USER);
  }

  public static Claims memberClaims(String email) {
    return claims(email, Role.MEMBER);
  }

  public static Claims adminClaims(String email) {
    return claims(email, Role.ADMIN);
  }

  public static Claims claims(String email, Role role) {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn(email);
    when(claims.get("rol", String.class)).thenReturn(role.name());
    return claims;
  }

  public static Claims invalidRoleClaims(String email) {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn(email);
    when(claims.get("rol", String.class)).thenReturn("INVALID_ROLE");
    return claims;
  }
}
