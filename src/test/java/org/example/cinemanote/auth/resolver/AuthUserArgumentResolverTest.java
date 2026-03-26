package org.example.cinemanote.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.cinemanote.auth.annotation.AuthUser;
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
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    @Mock UserRepository userRepository;
    @InjectMocks AuthUserArgumentResolver resolver;

    @Mock NativeWebRequest webRequest;
    @Mock HttpServletRequest httpServletRequest;
    @Mock ModelAndViewContainer mavContainer;
    @Mock WebDataBinderFactory binderFactory;

    // ─── supportsParameter ────────────────────────────────────

    @Test
    void supportsParameter_AuthUser_User타입_true() throws Exception {
        Method method = TestController.class.getMethod("withAuthUser", User.class);
        MethodParameter param = new MethodParameter(method, 0);

        assertThat(resolver.supportsParameter(param)).isTrue();
    }

    @Test
    void supportsParameter_AuthUser_없음_false() throws Exception {
        Method method = TestController.class.getMethod("withoutAnnotation", User.class);
        MethodParameter param = new MethodParameter(method, 0);

        assertThat(resolver.supportsParameter(param)).isFalse();
    }

    @Test
    void supportsParameter_AuthUser_있지만_타입이_다름_false() throws Exception {
        Method method = TestController.class.getMethod("withAuthUserWrongType", String.class);
        MethodParameter param = new MethodParameter(method, 0);

        assertThat(resolver.supportsParameter(param)).isFalse();
    }

    // ─── resolveArgument ──────────────────────────────────────

    @Test
    void resolveArgument_정상_User_반환() throws Exception {
        given(webRequest.getNativeRequest(HttpServletRequest.class)).willReturn(httpServletRequest);
        HttpSession session = mock(HttpSession.class);
        given(httpServletRequest.getSession(false)).willReturn(session);
        given(session.getAttribute(Const.SESSION_USER_KEY)).willReturn(1L);
        User user = User.of("a@b.com", "pw", "nick", UserRole.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        Object result = resolver.resolveArgument(null, mavContainer, webRequest, binderFactory);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void resolveArgument_session이_null이면_SESSION_NOT_FOUND() {
        given(webRequest.getNativeRequest(HttpServletRequest.class)).willReturn(httpServletRequest);
        given(httpServletRequest.getSession(false)).willReturn(null);

        assertThatThrownBy(() -> resolver.resolveArgument(null, mavContainer, webRequest, binderFactory))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    void resolveArgument_session에_userId_없으면_USERID_NOT_FOUND() {
        given(webRequest.getNativeRequest(HttpServletRequest.class)).willReturn(httpServletRequest);
        HttpSession session = mock(HttpSession.class);
        given(httpServletRequest.getSession(false)).willReturn(session);
        given(session.getAttribute(Const.SESSION_USER_KEY)).willReturn(null);

        assertThatThrownBy(() -> resolver.resolveArgument(null, mavContainer, webRequest, binderFactory))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USERID_NOT_FOUND));
    }

    @Test
    void resolveArgument_userId로_유저_미조회시_USER_NOT_FOUND() {
        given(webRequest.getNativeRequest(HttpServletRequest.class)).willReturn(httpServletRequest);
        HttpSession session = mock(HttpSession.class);
        given(httpServletRequest.getSession(false)).willReturn(session);
        given(session.getAttribute(Const.SESSION_USER_KEY)).willReturn(999L);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveArgument(null, mavContainer, webRequest, binderFactory))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // ─── 테스트용 더미 컨트롤러 (MethodParameter 생성에 사용) ──

    static class TestController {
        public void withAuthUser(@AuthUser User user) {}
        public void withoutAnnotation(User user) {}
        public void withAuthUserWrongType(@AuthUser String name) {}
    }
}
