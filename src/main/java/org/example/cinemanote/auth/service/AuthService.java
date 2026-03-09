package org.example.cinemanote.auth.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.cinemanote.auth.dto.SigninRequest;
import org.example.cinemanote.auth.dto.SignupRequest;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.Const;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.of(request.getEmail(), encodedPassword, request.getNickname(), UserRole.USER);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User signin(SigninRequest request, HttpSession session) {

        try {
            // Spring Security 표준 인증 플로우 사용
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 인증 성공 → SecurityContext 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션에 저장
            session.setAttribute(Const.SESSION_USER_KEY,
                    Long.parseLong(authentication.getName()));
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            return userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        } catch (BadCredentialsException e) {
            SecurityContextHolder.clearContext();
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
}