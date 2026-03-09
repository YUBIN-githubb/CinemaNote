package org.example.cinemanote.auth.resolver;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.cinemanote.auth.annotation.AuthUser;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.Const;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpSession session = webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class).getSession(false);

        if (session == null) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        Long userId = (Long) session.getAttribute(Const.SESSION_USER_KEY);

        if (userId == null) {
            throw new CustomException(ErrorCode.USERID_NOT_FOUND);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
