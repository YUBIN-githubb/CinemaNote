package org.example.cinemanote.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionTest {

    @Test
    void 생성자_ErrorCode만_전달() {
        CustomException ex = new CustomException(ErrorCode.ARCHIVE_NOT_FOUND);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ARCHIVE_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo(ErrorCode.ARCHIVE_NOT_FOUND.getMessage());
        assertThat(ex.getDetail()).isNull();
    }

    @Test
    void 생성자_ErrorCode와_detail_전달() {
        CustomException ex = new CustomException(ErrorCode.ARCHIVE_NOT_FOUND, "archiveId=99");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ARCHIVE_NOT_FOUND);
        assertThat(ex.getDetail()).isEqualTo("archiveId=99");
    }

    @Test
    void getHttpStatus_UNAUTHORIZED_401() {
        assertThat(new CustomException(ErrorCode.UNAUTHORIZED).getHttpStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getHttpStatus_FORBIDDEN_403() {
        assertThat(new CustomException(ErrorCode.FORBIDDEN).getHttpStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getHttpStatus_EMAIL_ALREADY_EXISTS_409() {
        assertThat(new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS).getHttpStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getHttpStatus_ARCHIVE_NOT_FOUND_404() {
        assertThat(new CustomException(ErrorCode.ARCHIVE_NOT_FOUND).getHttpStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getHttpStatus_SHARE_LINK_EXPIRED_410() {
        assertThat(new CustomException(ErrorCode.SHARE_LINK_EXPIRED).getHttpStatus())
                .isEqualTo(HttpStatus.GONE);
    }

    @Test
    void getHttpStatus_SHARE_LINK_INACTIVE_410() {
        assertThat(new CustomException(ErrorCode.SHARE_LINK_INACTIVE).getHttpStatus())
                .isEqualTo(HttpStatus.GONE);
    }

    @Test
    void getHttpStatus_INTERNAL_SERVER_ERROR_500() {
        assertThat(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR).getHttpStatus())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getHttpStatus_INVALID_PASSWORD_400() {
        assertThat(new CustomException(ErrorCode.INVALID_PASSWORD).getHttpStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
