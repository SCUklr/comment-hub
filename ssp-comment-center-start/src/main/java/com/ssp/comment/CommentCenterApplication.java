package com.ssp.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ssp.comment")
public class CommentCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentCenterApplication.class, args);
    }
}
