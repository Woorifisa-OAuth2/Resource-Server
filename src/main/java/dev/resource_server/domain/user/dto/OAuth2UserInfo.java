package dev.resource_server.domain.user.dto;

import lombok.Builder;
import org.springframework.security.oauth2.jwt.Jwt;

@Builder
public record OAuth2UserInfo(
    Long userId,
    String username,
    String name,
    Integer age,
    String gender,
    String email,
    String role
) {

    public static OAuth2UserInfo from(Jwt jwt) {
        return OAuth2UserInfo.builder()
                             .userId(jwt.getClaim("user_id"))
                             .username(jwt.getClaimAsString("username"))
                             .name(jwt.getClaimAsString("name"))
                             .age(jwt.getClaim("age"))
                             .gender(jwt.getClaimAsString("gender"))
                             .email(jwt.getClaimAsString("email"))
                             .role(jwt.getClaimAsString("role"))
                             .build();
    }
}