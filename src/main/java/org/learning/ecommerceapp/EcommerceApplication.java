package org.learning.ecommerceapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableCaching
public class EcommerceApplication {

    private static final Logger log = LoggerFactory.getLogger(EcommerceApplication.class);

    public static void main(String[] args) {
        log.info("Starting Ecommerce Application");
        SpringApplication.run(EcommerceApplication.class, args);
        log.info("Ecommerce Application started successfully");
    }
}