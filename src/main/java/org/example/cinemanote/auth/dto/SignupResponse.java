package org.example.cinemanote.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.UserRole;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long id;
    private String email;
    private String nickname;
    private UserRole userRole;

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
    }
}
