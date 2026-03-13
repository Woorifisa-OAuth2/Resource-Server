package dev.resource_server.global.security.filter;

import dev.resource_server.global.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 요청에서 jwt 출출
            String token = jwtUtils.getTokenFromHeader(request);

            // 2. 토큰 검증
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtUtils.isValidateToken(token) || jwtUtils.isExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 3. 토큰에서 userId 추출
            Long userId = jwtUtils.getUserIdFromToken(token);

            // 4. SecurityContext에 인증 정보 저장
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        null
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
