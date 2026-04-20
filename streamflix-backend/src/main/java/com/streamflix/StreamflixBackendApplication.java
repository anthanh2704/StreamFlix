package com.streamflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point of the Spring Boot application.

@SpringBootApplication
public class StreamflixBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamflixBackendApplication.class, args);
        System.out.println("""

                ╔════════════════════════════════════════════╗
                ║   StreamFlix API — http://localhost:8080   ║
                ║   Swagger JSON: /v3/api-docs (optional)    ║
                ╚════════════════════════════════════════════╝
                """);
    }
}
