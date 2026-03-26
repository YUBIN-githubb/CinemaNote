package org.example.cinemanote.domain.shareLink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.shareLink.dto.response.ShareLinkResponse;
import org.example.cinemanote.domain.shareLink.dto.response.SharedArchivesPageResponse;
import org.example.cinemanote.domain.shareLink.entity.ShareLink;
import org.example.cinemanote.domain.shareLink.service.ShareService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.Const;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ShareController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ShareControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ShareService shareService;
    @MockitoBean UserRepository userRepository;

    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        testUser = User.of("test@test.com", "pw", "tester", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        session = new MockHttpSession();
        session.setAttribute(Const.SESSION_USER_KEY, 1L);
    }

    // ─── POST /api/share ──────────────────────────────────────

    @Test
    void createShareLink_정상_생성_201() throws Exception {
        ShareLink link = ShareLink.of("test-token", testUser, null);
        given(shareService.createShareLink(any())).willReturn(ShareLinkResponse.from(link));

        mockMvc.perform(post("/api/share").session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shareToken").value("test-token"))
                .andExpect(jsonPath("$.data.shareUrl").value("/api/share/test-token"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    void createShareLink_미인증_세션없음_401() throws Exception {
        // 세션 없이 요청 → AuthUserArgumentResolver가 SESSION_NOT_FOUND(401) 발생
        mockMvc.perform(post("/api/share"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── DELETE /api/share ────────────────────────────────────

    @Test
    void deactivateShareLink_정상_비활성화_200() throws Exception {
        mockMvc.perform(delete("/api/share").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(shareService).deactivateShareLink(any());
    }

    @Test
    void deactivateShareLink_활성_링크_없음_404() throws Exception {
        org.mockito.Mockito.doThrow(new CustomException(ErrorCode.SHARE_LINK_NOT_FOUND))
                .when(shareService).deactivateShareLink(any());

        mockMvc.perform(delete("/api/share").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deactivateShareLink_미인증_세션없음_401() throws Exception {
        mockMvc.perform(delete("/api/share"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── GET /api/share/{shareToken} (인증 불필요) ────────────

    @Test
    void getSharedArchives_유효한_토큰_200() throws Exception {
        Archive archive = buildArchive(testUser);
        SharedArchivesPageResponse response = SharedArchivesPageResponse.of(
                "tester", new PageImpl<>(List.of(archive), PageRequest.of(0, 10), 1));
        given(shareService.getSharedArchives(eq("valid-token"), any())).willReturn(response);

        mockMvc.perform(get("/api/share/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getSharedArchives_존재하지_않는_토큰_404() throws Exception {
        given(shareService.getSharedArchives(eq("bad-token"), any()))
                .willThrow(new CustomException(ErrorCode.SHARE_LINK_NOT_FOUND));

        mockMvc.perform(get("/api/share/bad-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getSharedArchives_비활성_토큰_410() throws Exception {
        given(shareService.getSharedArchives(eq("inactive-token"), any()))
                .willThrow(new CustomException(ErrorCode.SHARE_LINK_INACTIVE));

        mockMvc.perform(get("/api/share/inactive-token"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getSharedArchives_만료된_토큰_410() throws Exception {
        given(shareService.getSharedArchives(eq("expired-token"), any()))
                .willThrow(new CustomException(ErrorCode.SHARE_LINK_EXPIRED));

        mockMvc.perform(get("/api/share/expired-token"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getSharedArchives_세션_없이도_정상_조회() throws Exception {
        SharedArchivesPageResponse response = SharedArchivesPageResponse.of(
                "tester", new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
        given(shareService.getSharedArchives(any(), any())).willReturn(response);

        // @AuthUser 파라미터가 없는 엔드포인트이므로 세션 불필요
        mockMvc.perform(get("/api/share/some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─── 헬퍼 메서드 ─────────────────────────────────────────

    private Archive buildArchive(User user) {
        return Archive.of(user, 1L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "overview", LocalDate.of(2010, 7, 16), 8.0f, "good");
    }
}
