# OAuth 2.0 Resource Server

자체 OAuth 2.0 인가 서버(Spring Authorization Server)와 연동되는 리소스 서버입니다.
인가 서버에서 발급한 JWT를 검증하고, 사용자 정보를 추출하여 자체 JWT를 재발급하는 이중 토큰 구조로 동작합니다.

## 시스템 아키텍처

```
Frontend (React/Next.js)       Resource Server (Spring Boot)       Authorization Server
    │                                │                                    │
    │  GET /api/auth/code            │                                    │
    │───────────────────────────────>│                                    │
    │  302 Redirect ────────────────>│───────────────────────────────────>│
    │                                │              로그인 + 동의         │
    │<───────────────────────────────│<─────── code callback ─────────────│
    │                                │                                    │
    │  POST /api/auth/login          │                                    │
    │   { "code": "xxx" }            │                                    │
    │───────────────────────────────>│  code → token 교환                 │
    │                                │───────────────────────────────────>│
    │                                │<── Access Token (JWT) ─────────────│
    │                                │                                    │
    │                                │  JWT 디코딩 → 사용자 저장          │
    │                                │  자체 JWT 발급                     │
    │                                │                                    │
    │<── { "access_token": "xxx" } ──│                                    │
    │                                │                                    │
    │  이후 API 요청                 │                                    │
    │  Authorization: Bearer <token> │                                    │
    │───────────────────────────────>│  JwtAuthenticationFilter           │
    │                                │  자체 JWT 검증 → SecurityContext   │
```

## 토큰 전략

인가 서버의 JWT(사용자 상세 정보 포함)를 프론트엔드에 직접 전달하지 않습니다. 대신 최소한의 claim(`userId`)만 담은 자체 JWT를 발급하여 전달합니다. 인가 서버의 Access Token과 Refresh Token은 리소스 서버 DB에만 보관됩니다.

이 구조를 통해 사용자 민감 정보가 프론트엔드에 노출되지 않으며, 인가 서버 장애 여부와 무관하게 리소스 서버가 독립적으로 인증을 처리할 수 있습니다.

1. 인가 서버 JWT를 `NimbusJwtDecoder`로 서명 검증 및 디코딩
2. claims에서 사용자 정보(email, role, age 등)를 추출하여 DB에 저장
3. 리소스 서버 자체의 HMAC 기반 JWT를 발급하여 프론트엔드에 반환 (claim: `userId`만 포함)
4. 자체 JWT 만료 시간은 인가 서버 Access Token의 `exp` claim에 맞춤

## 기술 스택

| 구분 | 기술 |
|---|---|
| Framework | Spring Boot 3.5, Spring Security 6.5 |
| Auth | Spring OAuth2 Client, NimbusJwtDecoder, jjwt 0.12 |
| DB | MySQL 8.0, Spring Data JPA, Hibernate 6.6 |
| Build | Gradle, Java 17 |

## API 명세

### 인증 (공개 엔드포인트)

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/auth/code` | 인가 서버 로그인 페이지로 redirect |
| POST | `/api/auth/login` | authorization code → 자체 JWT 교환 |

**POST /api/auth/login**

```json
// Request Body
{ "code": "authorization_code_value" }

// Response
{ "access_token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### 사용자 (인증 필요)

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/user` | 현재 로그인한 사용자 정보 조회 |

요청 시 `Authorization: Bearer <자체_JWT>` 헤더 필요.

## 실행 방법

### 사전 요구사항

- Java 17
- MySQL 8.0
- 인가 서버(Spring Authorization Server) 실행 중

### 1. 데이터베이스 생성

```sql
CREATE DATABASE testdb;
```

### 2. 설정 파일 수정

`src/main/resources/application-dev.yaml`에서 환경에 맞게 수정:

```yaml
auth:
  server:
    registration-uri: http://{AUTH_SERVER_IP}:9000/api/clients
    authorization-uri: http://{AUTH_SERVER_IP}:9000/oauth2/authorize
    token-uri: http://{AUTH_SERVER_IP}:9000/oauth2/token
    redirect-uri: http://{FRONTEND_IP}:3000/callback
    jwk-set-uri: http://{AUTH_SERVER_IP}:9000/oauth2/jwks

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb
    username: {DB_USERNAME}
    password: {DB_PASSWORD}

jwt:
  secret: {최소 32자 이상의 비밀키}

frontend:
  url: http://{FRONTEND_IP}:3000
```

### 3. 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

서버 시작 시 `AuthClientInitializer`가 인가 서버에 클라이언트를 자동 등록합니다.

## 핵심 구현 상세

### 클라이언트 자동 등록

`AuthClientInitializer`는 `ApplicationRunner`를 구현하여 서버 시작 시 인가 서버의 동적 클라이언트 등록 엔드포인트(`/api/clients`)에 자동으로 클라이언트를 등록합니다. 발급받은 `client_id`와 `client_secret`은 DB에 저장됩니다.

### Authorization Code Flow (수동 구현)

Spring Security의 자동 OAuth2 Login을 사용하지 않고,
`RestClientAuthorizationCodeTokenResponseClient`를 직접 활용하여
code → token 교환을 수동 구현했습니다.

- `ClientRegistration`: Auth Server 메타정보(clientId, clientSecret, tokenUri 등)를 프로그래밍 방식으로 구성
- `OAuth2AuthorizationExchange`: 최초 code 요청과 응답을 묶어 redirect_uri 일치 검증 수행
- `RestClientAuthorizationCodeTokenResponseClient`: RFC 6749 스펙에 맞게 token 교환 HTTP 요청을 자동 처리

> 자동 구성이 불가능한 커스텀 Auth Server와의 통신을 위해 Spring OAuth2 내부 자료구조를 직접 조립했습니다.


### 인가 서버 JWT 검증(RS256 + JWKS)
인가 서버는 JWT를 RS256(비대칭키) 알고리즘으로 서명합니다.
리소스 서버는 인가 서버의 [JWKS(JSON Web Key Set)](https://datatracker.ietf.org/doc/html/rfc7517) 엔드포인트(/oauth2/jwks)에서 공개키를 가져와 서명을 검증합니다.

- 키 교체(rotation) 시 엔드포인트에서 새 키를 자동으로 가져오므로 수동 개입 불필요
- `spring.security.oauth2.resourceserver` 프로퍼티를 설정하지 않아 Spring Boot Resource Server 자동 구성을 비활성화 → JWT 검증 로직을 직접 제어
- `NimbusJwtDecoder`를 Bean으로 직접 등록하여 `OAuth2LoginSuccessHandler`에서 명시적으로 사용

### Stateless 인증(JwtAuthenticationFilter)
`OncePerRequestFilter`를 상속한 `JwtAuthenticationFilter`가 매 요청마다 자체 JWT를 검증합니다.

- 세션을 생성하지 않는 `STATELESS` 정책 적용
- JWT 검증 성공 시 `userId`를 `SecurityContextHolder`에 저장
- 이후 컨트롤러에서 `@AuthenticationPrincipal Long userId`로 바로 사용 가능
- `/api/auth/**` 경로는 인증 없이 허용 (로그인, 토큰 발급)
