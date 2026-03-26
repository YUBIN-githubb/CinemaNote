package org.example.cinemanote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class CinemaNoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaNoteApplication.class, args);
    }

}
