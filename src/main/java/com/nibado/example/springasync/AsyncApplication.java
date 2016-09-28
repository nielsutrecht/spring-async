package com.nibado.example.springasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AsyncApplication {
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(AsyncApplication.class, args);
    }
}
