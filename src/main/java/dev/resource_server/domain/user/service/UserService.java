package dev.resource_server.domain.user.service;

import dev.resource_server.domain.user.domain.User;
import dev.resource_server.domain.user.dto.UserResponse;
import dev.resource_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(String authId) {
        User user = userRepository.findByAuthId(authId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        return new UserResponse(user);
    }
}