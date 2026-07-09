package com.bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point for the eCommerce Bookstore application.
 *
 * <p>Extends {@link SpringBootServletInitializer} so the application can be
 * packaged as a WAR and deployed on IBM WebSphere Liberty, while still being
 * runnable as a standard JAR for local development.</p>
 */
@SpringBootApplication
public class BookstoreApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BookstoreApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }
}
