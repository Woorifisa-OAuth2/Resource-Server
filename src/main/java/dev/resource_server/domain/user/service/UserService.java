package dev.resource_server.domain.user.service;

import dev.resource_server.domain.user.domain.User;
import dev.resource_server.domain.user.dto.OAuth2UserInfo;
import dev.resource_server.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    /**
     * 로그인 시 사용자 조회 또는 생성
     */
    public User findOrCreateUser(OAuth2UserInfo userInfo) {
        return userRepository.findByAuthId(userInfo.userId())
                             .orElseGet(() -> createUser(userInfo));
    }

    /**
     * 새로운 사용자 생성
     */
    private User createUser(OAuth2UserInfo userInfo) {
        User user = User.builder()
                        .authId(userInfo.userId())
                        .username(userInfo.username())
                        .name(userInfo.name())
                        .age(userInfo.age())
                        .gender(userInfo.gender())
                        .email(userInfo.email())
                        .role(userInfo.role())
                        .build();

        return userRepository.save(user);
    }

    public void saveOrUpdateToken(User user, String accessToken, String refreshToken) {
        user.updateTokens(accessToken, refreshToken);
    }
}
