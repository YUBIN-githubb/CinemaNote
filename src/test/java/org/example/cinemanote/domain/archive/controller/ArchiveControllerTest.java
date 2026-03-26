package org.example.cinemanote.domain.archive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.archive.dto.response.ArchiveResponse;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.archive.service.ArchiveService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.domain.user.repository.UserRepository;
import org.example.cinemanote.global.common.Const;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.example.cinemanote.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(
        controllers = ArchiveController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ArchiveControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ArchiveService archiveService;
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

    // ─── GET /api/archives ────────────────────────────────────

    @Test
    void getArchives_정상_조회() throws Exception {
        Archive archive = buildArchive(1L, testUser);
        PageResponse<ArchiveResponse> page = PageResponse.from(
                new PageImpl<>(List.of(ArchiveResponse.from(archive)), PageRequest.of(0, 10), 1));
        given(archiveService.getArchives(any(), any())).willReturn(page);

        mockMvc.perform(get("/api/archives").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getArchives_미인증_세션없음_401() throws Exception {
        // 세션 없이 요청 → AuthUserArgumentResolver가 SESSION_NOT_FOUND(401) 발생
        mockMvc.perform(get("/api/archives"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── GET /api/archives/{id} ───────────────────────────────

    @Test
    void getArchive_정상_조회() throws Exception {
        Archive archive = buildArchive(1L, testUser);
        given(archiveService.getArchive(any(), eq(1L))).willReturn(ArchiveResponse.from(archive));

        mockMvc.perform(get("/api/archives/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Inception"));
    }

    @Test
    void getArchive_존재하지_않는_ID_404() throws Exception {
        given(archiveService.getArchive(any(), eq(999L)))
                .willThrow(new CustomException(ErrorCode.ARCHIVE_NOT_FOUND));

        mockMvc.perform(get("/api/archives/999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getArchive_타인_아카이브_403() throws Exception {
        given(archiveService.getArchive(any(), eq(2L)))
                .willThrow(new CustomException(ErrorCode.ARCHIVE_ACCESS_DENIED));

        mockMvc.perform(get("/api/archives/2").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /api/archives ───────────────────────────────────

    @Test
    void createArchive_정상_생성_201() throws Exception {
        Archive archive = buildArchive(1L, testUser);
        given(archiveService.createArchive(any(), any())).willReturn(Mono.just(ArchiveResponse.from(archive)));

        // Mono 반환 컨트롤러는 Spring MVC가 비동기로 처리한다.
        // perform()은 AsyncContext 시작 시점에 리턴되므로 응답 바디가 아직 비어있다.
        // asyncDispatch()로 Mono 구독 완료 후 응답을 검증해야 한다.
        MvcResult asyncResult = mockMvc.perform(post("/api/archives").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("tmdbId", 123, "contentType", "MOVIE",
                                        "rating", 8.0, "review", "good"))))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createArchive_중복_409() throws Exception {
        given(archiveService.createArchive(any(), any()))
                .willThrow(new CustomException(ErrorCode.ARCHIVE_ALREADY_EXISTS));

        mockMvc.perform(post("/api/archives").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("tmdbId", 123, "contentType", "MOVIE",
                                        "rating", 8.0, "review", "good"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createArchive_미인증_401() throws Exception {
        // 세션 없이 요청 → SESSION_NOT_FOUND(401)
        mockMvc.perform(post("/api/archives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ─── DELETE /api/archives/{id} ────────────────────────────

    @Test
    void deleteArchive_정상_삭제_200() throws Exception {
        mockMvc.perform(delete("/api/archives/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(archiveService).deleteArchive(any(), eq(1L));
    }

    @Test
    void deleteArchive_존재하지_않는_ID_404() throws Exception {
        org.mockito.Mockito.doThrow(new CustomException(ErrorCode.ARCHIVE_NOT_FOUND))
                .when(archiveService).deleteArchive(any(), eq(999L));

        mockMvc.perform(delete("/api/archives/999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteArchive_타인_아카이브_403() throws Exception {
        org.mockito.Mockito.doThrow(new CustomException(ErrorCode.ARCHIVE_ACCESS_DENIED))
                .when(archiveService).deleteArchive(any(), eq(2L));

        mockMvc.perform(delete("/api/archives/2").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── PATCH /api/archives/{id} ─────────────────────────────

    @Test
    void updateArchive_정상_수정_200() throws Exception {
        Archive updated = buildArchive(1L, testUser);
        updated.update(9.5f, "수정된 리뷰");
        given(archiveService.updateArchive(any(), eq(1L), any())).willReturn(ArchiveResponse.from(updated));

        mockMvc.perform(patch("/api/archives/1").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("rating", 9.5, "review", "수정된 리뷰"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(9.5))
                .andExpect(jsonPath("$.data.review").value("수정된 리뷰"));
    }

    @Test
    void updateArchive_rating_범위초과_400() throws Exception {
        mockMvc.perform(patch("/api/archives/1").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("rating", 11.0, "review", "리뷰"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateArchive_rating_음수_400() throws Exception {
        mockMvc.perform(patch("/api/archives/1").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("rating", -1.0, "review", "리뷰"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 헬퍼 메서드 ─────────────────────────────────────────

    private Archive buildArchive(Long id, User user) {
        Archive archive = Archive.of(user, 123L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "overview", LocalDate.of(2010, 7, 16), 8.0f, "good");
        ReflectionTestUtils.setField(archive, "id", id);
        return archive;
    }
}
