package org.example;

//import org.example.entities.environment.EnvironmentData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GreenDesk {

    public static void main(String[] args) {
        SpringApplication.run(GreenDesk.class, args);
    }
}
