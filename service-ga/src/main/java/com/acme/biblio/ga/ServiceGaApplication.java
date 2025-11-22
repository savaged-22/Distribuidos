package com.acme.biblio.ga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceGaApplication {
  public static void main(String[] args) {
    SpringApplication.run(ServiceGaApplication.class, args);
  }
}
