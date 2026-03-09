package org.example.cinemanote.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.cinemanote.global.common.Const;

@Getter
@AllArgsConstructor
public class SignupRequest {

    @Email
    private String email;

    @Pattern(regexp = Const.PASSWORD_PATTERN)
    private String password;

    @NotBlank
    private String nickname;
}
