package dev.resource_server.global.security.hanlder;

import dev.resource_server.domain.user.domain.User;
import dev.resource_server.domain.user.dto.OAuth2UserInfo;
import dev.resource_server.domain.user.repository.UserRepository;
import dev.resource_server.domain.user.service.UserService;
import dev.resource_server.global.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler {

    private final JwtUtils jwtUtils;
    private final JwtDecoder jwtDecoder;
    private final UserService userService;


    public String onSuccess(OAuth2AccessTokenResponse authentication) {
        log.debug("OAuth2 로그인 성공");

        // 1. Access Token에서 JWT 디코딩
        String accessToken = authentication.getAccessToken().getTokenValue();
        Jwt jwt = jwtDecoder.decode(accessToken);

        // 2. claim → DTO 변환
        OAuth2UserInfo userInfo = OAuth2UserInfo.from(jwt);

        // 3. User 조회 또는 생성
        User user = userService.findOrCreateUser(userInfo);
        log.debug("OAuth2 로그인 성공: username={}", user.getName());

        // 4. 인가 서버 토큰 저장 (갱신 시 사용)
        String refreshToken = null;
        if (authentication.getRefreshToken() != null) {
            refreshToken = authentication.getRefreshToken().getTokenValue();
        }
        userService.saveOrUpdateToken(user, accessToken, refreshToken);

        // 5. 자체 JWT 발급 (리소스 서버용)
        Instant authTokenExpired = jwt.getExpiresAt();
        return jwtUtils.createToken(user.getId(), authTokenExpired);
    }
}
