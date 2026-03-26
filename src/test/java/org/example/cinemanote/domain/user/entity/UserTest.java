package org.example.cinemanote.domain.user.entity;

import org.example.cinemanote.global.common.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void of_모든_필드_정상_설정() {
        User user = User.of("test@test.com", "encodedPw", "tester", UserRole.USER);

        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPassword()).isEqualTo("encodedPw");
        assertThat(user.getNickname()).isEqualTo("tester");
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void of_ADMIN_역할_설정() {
        User user = User.of("admin@test.com", "pw", "admin", UserRole.ADMIN);

        assertThat(user.getUserRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeNickname_닉네임_변경() {
        User user = User.of("test@test.com", "pw", "oldNick", UserRole.USER);

        user.changeNickname("newNick");

        assertThat(user.getNickname()).isEqualTo("newNick");
    }

    @Test
    void changePassword_비밀번호_변경() {
        User user = User.of("test@test.com", "oldPw", "nick", UserRole.USER);

        user.changePassword("newPw");

        assertThat(user.getPassword()).isEqualTo("newPw");
    }

    @Test
    void changeNickname_기존_다른_필드는_변경되지_않는다() {
        User user = User.of("test@test.com", "pw", "oldNick", UserRole.USER);

        user.changeNickname("newNick");

        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPassword()).isEqualTo("pw");
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
    }
}
