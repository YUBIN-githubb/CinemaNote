package org.example.cinemanote.global.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void ok_data만_전달() {
        ApiResponse<String> response = ApiResponse.ok("hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void ok_message와_data_전달() {
        ApiResponse<String> response = ApiResponse.ok("생성 성공", "hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("생성 성공");
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void ok_data가_null() {
        ApiResponse<Void> response = ApiResponse.ok(null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }

    @Test
    void fail_message_전달() {
        ApiResponse<Void> response = ApiResponse.fail("오류 발생");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("오류 발생");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void timestamp는_현재_시각으로_설정된다() {
        ApiResponse<Void> response = ApiResponse.ok(null);
        assertThat(response.getTimestamp()).isNotNull();
    }
}
