package com.teamrhythm.concertwishlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ConcertWishlistApplication {
    private static final Logger log = LoggerFactory.getLogger(ConcertWishlistApplication.class);

    public static void main(String[] args) {
        log.info("Starting ConcertWishlistApplication");
        SpringApplication.run(ConcertWishlistApplication.class, args);
        log.info("ConcertWishlistApplication started");
    }
}
