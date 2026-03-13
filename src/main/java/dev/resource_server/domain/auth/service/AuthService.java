package dev.resource_server.domain.auth.service;

import dev.resource_server.domain.auth.dto.TokenRequest;
import dev.resource_server.domain.auth.dto.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestClientAuthorizationCodeTokenResponseClient tokenResponseClient;

    @Value("${auth.server.authorization-uri}")
    private String authorizationUri;

    @Value("${auth.server.token-uri}")
    private String tokenUri;

    @Value("${auth.server.client-id}")
    private String clientId;

    @Value("${auth.server.client-secret}")
    private String clientSecret;

    @Value("${auth.server.grant-type}")
    private String grantType;

    @Value("${auth.server.redirect-uri}")
    private String redirectUri;

    @Value("${auth.server.scope}")
    private String scope;

    public void authorize(HttpServletResponse response) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    public TokenResponse getToken(TokenRequest request) {
        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId("auth-server")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(new AuthorizationGrantType(grantType))
                .redirectUri(redirectUri)
                .tokenUri(tokenUri)
                .authorizationUri(authorizationUri)
                .build();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(clientId)
                .authorizationUri(authorizationUri)
                .redirectUri(redirectUri)
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse
                .success(request.getCode())
                .redirectUri(redirectUri)
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                clientRegistration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );

        OAuth2AccessTokenResponse tokenResponse = tokenResponseClient.getTokenResponse(grantRequest);

        // Todo: DB 처리

        return TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken().getTokenValue())
                .build();
    }
}