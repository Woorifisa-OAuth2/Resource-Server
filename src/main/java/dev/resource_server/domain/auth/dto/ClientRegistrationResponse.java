package dev.resource_server.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientRegistrationResponse {

    private String clientId;
    private String clientSecret;
}