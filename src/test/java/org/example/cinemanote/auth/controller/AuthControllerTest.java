package org.example.cinemanote.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cinemanote.auth.service.AuthService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// SecurityAutoConfiguration을 제외해야 하는 이유:
// @WebMvcTest는 @Configuration @EnableWebSecurity인 SecurityConfig를 스캔 대상에서 제외한다.
// 그 결과 Spring Boot 기본 보안 설정(CSRF ON, 전체 인증 필요)이 적용되어 POST 요청이 403을 반환한다.
// 컨트롤러 단위 테스트에서는 Spring Security를 제외하고, 미인증은 AuthUserArgumentResolver가 처리한다.
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean UserRepository userRepository; // AuthUserArgumentResolver 의존성

    private User buildUser() {
        return User.of("test@test.com", "encoded", "tester", UserRole.USER);
    }

    // ─── POST /signup ─────────────────────────────────────────

    @Test
    void signup_정상_요청() throws Exception {
        given(authService.signup(any())).willReturn(buildUser());

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@test.com",
                                        "password", "Password1!",
                                        "nickname", "tester"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.userRole").value("USER"));
    }

    @Test
    void signup_nickname_빈값_400() throws Exception {
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@test.com",
                                        "password", "Password1!",
                                        "nickname", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_email_형식_오류_400() throws Exception {
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "not-an-email",
                                        "password", "Password1!",
                                        "nickname", "tester"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_password_패턴_불일치_400() throws Exception {
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@test.com",
                                        "password", "weak",
                                        "nickname", "tester"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_이메일_중복_409() throws Exception {
        given(authService.signup(any())).willThrow(new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "dup@test.com",
                                        "password", "Password1!",
                                        "nickname", "tester"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다"));
    }

    @Test
    void signup_빈_body_400() throws Exception {
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /signin ─────────────────────────────────────────

    @Test
    void signin_정상_로그인() throws Exception {
        given(authService.signin(any(), any())).willReturn(buildUser());

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@test.com", "password", "Password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));
    }

    @Test
    void signin_비밀번호_불일치_400() throws Exception {
        given(authService.signin(any(), any())).willThrow(new CustomException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@test.com", "password", "wrong"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signin_email_빈값_400() throws Exception {
        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "", "password", "Password1!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /signout ────────────────────────────────────────

    @Test
    void signout_정상_로그아웃() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/signout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        assert session.isInvalid();
    }
}
