package dev.resource_server.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long authId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(length = 2000)
    private String authAccessToken;

    @Column(length = 2000)
    private String authRefreshToken;

    public void updateTokens(String accessToken, String refreshToken) {
        this.authAccessToken = accessToken;
        if (refreshToken != null) {
            this.authRefreshToken = refreshToken;
        }
    }
}