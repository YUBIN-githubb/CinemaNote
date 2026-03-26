package org.example.cinemanote.global.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void from_정상_변환() {
        List<String> content = List.of("a", "b", "c");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageable, 3);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getContent()).containsExactly("a", "b", "c");
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    @Test
    void from_마지막_페이지가_아닌_경우() {
        List<String> content = List.of("a", "b");
        PageRequest pageable = PageRequest.of(0, 2);
        Page<String> page = new PageImpl<>(content, pageable, 10);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.isLast()).isFalse();
        assertThat(response.getTotalPages()).isEqualTo(5);
    }

    @Test
    void from_빈_페이지() {
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.isLast()).isTrue();
    }

    @Test
    void from_2번째_페이지() {
        List<Integer> content = List.of(11, 12);
        PageRequest pageable = PageRequest.of(1, 10);
        Page<Integer> page = new PageImpl<>(content, pageable, 22);

        PageResponse<Integer> response = PageResponse.from(page);

        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.isLast()).isFalse();
        assertThat(response.getTotalPages()).isEqualTo(3);
    }
}
