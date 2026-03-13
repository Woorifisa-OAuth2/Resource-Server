package dev.resource_server.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClientRegistrationRequest {

    private String clientName;
    private String redirectUri;
    private List<String> scopes;
}