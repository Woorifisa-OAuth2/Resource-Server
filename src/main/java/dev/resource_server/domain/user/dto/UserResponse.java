package dev.resource_server.domain.user.dto;

import dev.resource_server.domain.user.domain.User;
import lombok.Getter;

@Getter
public class UserResponse {

    private final String name;
    private final String age;
    private final String gender;

    public UserResponse(User user) {
        this.name = user.getName();
        this.age = user.getAge();
        this.gender = user.getGender();
    }
}