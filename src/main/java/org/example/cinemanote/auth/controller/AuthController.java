package org.example.cinemanote.auth.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cinemanote.auth.dto.SigninRequest;
import org.example.cinemanote.auth.dto.SigninResponse;
import org.example.cinemanote.auth.dto.SignupRequest;
import org.example.cinemanote.auth.dto.SignupResponse;
import org.example.cinemanote.auth.service.AuthService;
import org.example.cinemanote.global.response.ApiResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(SignupResponse.from(authService.signup(request)));
    }

    @PostMapping("/signin")
    public ApiResponse<SigninResponse> signin(
            @Valid @RequestBody SigninRequest request,
            HttpSession session
    ) {
        return ApiResponse.ok(SigninResponse.from(authService.signin(request, session)));
    }

    @PostMapping("/signout")
    public ApiResponse<Void> signout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ApiResponse.ok(null);
    }
}
