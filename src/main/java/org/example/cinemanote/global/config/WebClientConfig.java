package org.example.cinemanote.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${tmdb.base-url}")
    private String tmdbBaseUrl;

    @Bean
    public WebClient tmdbWebClient() {

        // ① 연결 풀 설정
        ConnectionProvider connectionProvider = ConnectionProvider.builder("tmdb-pool")
                .maxConnections(50)              // 최대 동시 연결 수
                .pendingAcquireMaxCount(100)     // 연결 대기 큐 크기
                .maxIdleTime(Duration.ofSeconds(20))  // 유휴 연결 정리 시간
                .maxLifeTime(Duration.ofSeconds(60))  // 연결 최대 수명
                .build();

        // ② 타임아웃 설정
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)  // TCP 연결 타임아웃
                .responseTimeout(Duration.ofSeconds(5))              // 응답 대기 타임아웃
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5))   // 읽기 타임아웃
                        .addHandlerLast(new WriteTimeoutHandler(5))  // 쓰기 타임아웃
                );

        return WebClient.builder()
                .baseUrl(tmdbBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // ③ 메모리 제한
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024))  // 2MB로 제한
                .build();
    }
}
