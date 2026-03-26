package org.example.cinemanote.auth.service;

import jakarta.servlet.http.HttpSession;
import org.example.cinemanote.auth.dto.SigninRequest;
import org.example.cinemanote.auth.dto.SignupRequest;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.Const;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks AuthService authService;

    // ─── signup ───────────────────────────────────────────────

    @Test
    void signup_정상_회원가입() {
        SignupRequest req = new SignupRequest("a@b.com", "Password1!", "nick");
        given(userRepository.existsByEmail("a@b.com")).willReturn(false);
        given(passwordEncoder.encode("Password1!")).willReturn("encoded");
        User saved = User.of("a@b.com", "encoded", "nick", UserRole.USER);
        given(userRepository.save(any())).willReturn(saved);

        User result = authService.signup(req);

        assertThat(result.getEmail()).isEqualTo("a@b.com");
        assertThat(result.getNickname()).isEqualTo("nick");
        assertThat(result.getUserRole()).isEqualTo(UserRole.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_비밀번호가_인코딩되어_저장된다() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode("Password1!")).willReturn("encoded");
        given(userRepository.save(any())).willReturn(User.of("a@b.com", "encoded", "nick", UserRole.USER));

        authService.signup(new SignupRequest("a@b.com", "Password1!", "nick"));

        verify(passwordEncoder).encode("Password1!");
    }

    @Test
    void signup_이메일_중복_예외() {
        given(userRepository.existsByEmail("a@b.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("a@b.com", "Password1!", "nick")))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));

        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_저장된_유저의_역할은_USER() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        User result = authService.signup(new SignupRequest("a@b.com", "Password1!", "nick"));

        assertThat(result.getUserRole()).isEqualTo(UserRole.USER);
    }

    // ─── signin ───────────────────────────────────────────────

    @Test
    void signin_정상_로그인() {
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("1");
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);
        User user = User.of("a@b.com", "encoded", "nick", UserRole.USER);
        given(userRepository.findByEmail("a@b.com")).willReturn(Optional.of(user));
        HttpSession session = mock(HttpSession.class);

        User result = authService.signin(new SigninRequest("a@b.com", "pass"), session);

        assertThat(result).isEqualTo(user);
        verify(session).setAttribute(eq(Const.SESSION_USER_KEY), any());
    }

    @Test
    void signin_세션에_userId가_저장된다() {
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("42");
        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(userRepository.findByEmail(any())).willReturn(
                Optional.of(User.of("a@b.com", "encoded", "nick", UserRole.USER)));
        HttpSession session = mock(HttpSession.class);

        authService.signin(new SigninRequest("a@b.com", "pass"), session);

        verify(session).setAttribute(eq(Const.SESSION_USER_KEY), eq(42L));
    }

    @Test
    void signin_비밀번호_불일치_예외() {
        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("bad credentials"));
        HttpSession session = mock(HttpSession.class);

        assertThatThrownBy(() -> authService.signin(new SigninRequest("a@b.com", "wrong"), session))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    void signin_기타_인증_실패_예외() {
        given(authenticationManager.authenticate(any())).willThrow(new AuthenticationException("fail") {});
        HttpSession session = mock(HttpSession.class);

        assertThatThrownBy(() -> authService.signin(new SigninRequest("a@b.com", "pass"), session))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.AUTHENTICATION_FAILED));
    }
}
