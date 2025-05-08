
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "entity")
@ComponentScan(basePackages = { "controller", "repository", "entity" })
public class Game2048ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(Game2048ServerApplication.class, args);
    }
}
