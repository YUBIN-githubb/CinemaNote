package org.example.cinemanote.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.UserRole;

@Getter
@AllArgsConstructor
public class SigninResponse {

    private Long id;
    private String email;
    private String nickname;
    private UserRole userRole;

    public static SigninResponse from(User user) {
        return new SigninResponse(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
    }
}
