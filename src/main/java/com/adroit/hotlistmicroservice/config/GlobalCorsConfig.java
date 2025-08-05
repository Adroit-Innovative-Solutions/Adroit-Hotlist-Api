package com.adroit.hotlistmicroservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // allow all endpoints
                        .allowedOrigins(
                                "http://35.188.150.92",  // First IP
                                "http://192.168.0.140:3000",  // Second IP
                                "http://192.168.0.139:3000", // Third IP
                                "https://mymulya.com", // Forth IP
                                "http://localhost:3000", // Fifth IP
                                "http://192.168.0.135/",
                                "http://192.168.0.135:80","http://182.18.177.16:443",
                                "http://mymulya.com:443",
                                "http://localhost/",
                                "http://182.18.177.16/" // Sixth IP
                        )                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // allowed HTTP methods
                        .allowedHeaders("*") // allow all headers
                        .allowCredentials(true); // if you're using cookies/auth
            }
        };
    }
}
