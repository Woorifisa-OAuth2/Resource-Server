package dev.resource_server.domain.auth.service;

import dev.resource_server.domain.auth.domain.Client;
import dev.resource_server.domain.auth.dto.ClientRegistrationRequest;
import dev.resource_server.domain.auth.dto.ClientRegistrationResponse;
import dev.resource_server.domain.auth.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClientInitializer implements ApplicationRunner {

    private final ClientRepository clientRepository;

    @Value("${auth.server.registration-uri}")
    private String registrationUri;

    @Value("${auth.server.redirect-uri}")
    private String redirectUri;

    @Value("${auth.server.scope}")
    private String scope;

    private static final String CLIENT_NAME = "my-app";

    @Override
    public void run(ApplicationArguments args) {
        log.info("앱 등록 시작");
        if (clientRepository.existsByClientName(CLIENT_NAME)) {
            return;
        }

        ClientRegistrationRequest request = new ClientRegistrationRequest(
                CLIENT_NAME,
                redirectUri,
                List.of(scope.split(" "))
        );

        ClientRegistrationResponse response = RestClient.create()
                .post()
                .uri(registrationUri)
                .body(request)
                .retrieve()
                .body(ClientRegistrationResponse.class);

        Client client = Client.builder()
                .clientName(CLIENT_NAME)
                .clientId(response.getClientId())
                .clientSecret(response.getClientSecret())
                .build();

        clientRepository.save(client);
        log.info("앱 등록 완료");
    }
}