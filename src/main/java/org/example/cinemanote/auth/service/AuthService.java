package org.example.cinemanote.auth.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.cinemanote.auth.dto.SigninRequest;
import org.example.cinemanote.auth.dto.SignupRequest;
import org.example.cinemanote.auth.util.PasswordEncoder;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.Const;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        session.setAttribute(Const.SESSION_USER_KEY, user.getId());

        // Spring Security 인증 정보도 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId(),      // principal
                null,              // credentials (비밀번호는 넣지 않음)
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 세션에 SecurityContext 저장
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        return user;
    }
}
