package dev.resource_server.domain.auth.api;

import dev.resource_server.domain.auth.dto.TokenRequest;
import dev.resource_server.domain.auth.dto.TokenResponse;
import dev.resource_server.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final AuthService authService;

    @GetMapping("/code")
    public void authorize(HttpServletResponse response) throws IOException {
        log.info("code 요청");
        authService.authorize(response);
        log.info("code 발급완료");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> getToken(@RequestBody TokenRequest request) {
        log.info("토큰 발급 요청 code = {}", request.getCode());
        TokenResponse tokenResponse = authService.getToken(request);
        log.info("토큰 발급 완료");
        return ResponseEntity.ok(tokenResponse);
    }
}