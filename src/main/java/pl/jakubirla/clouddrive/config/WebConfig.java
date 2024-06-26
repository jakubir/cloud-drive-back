package pl.jakubirla.clouddrive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION)
                        .allowCredentials(true)
                        .exposedHeaders(HttpHeaders.CONTENT_DISPOSITION);
            }
        };
    }
}
