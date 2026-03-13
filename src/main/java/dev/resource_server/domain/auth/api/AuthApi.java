package dev.resource_server.domain.auth.api;

import dev.resource_server.domain.auth.dto.TokenRequest;
import dev.resource_server.domain.auth.dto.TokenResponse;
import dev.resource_server.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final AuthService authService;

    @GetMapping("/code")
    public void authorize(HttpServletResponse response) throws IOException {
        authService.authorize(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> getToken(@RequestBody TokenRequest request) {
        TokenResponse tokenResponse = authService.getToken(request);
        return ResponseEntity.ok(tokenResponse);
    }
}