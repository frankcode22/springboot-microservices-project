package com.citizensciencewater.auth.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

 @Override
 public void addCorsMappings(CorsRegistry registry) {
     registry.addMapping("/**") // Apply to all endpoints
             .allowedOrigins("*") // 👈 ALLOWS ALL ORIGINS
             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
             .allowedHeaders("*") 
             .allowCredentials(false); // ✅ if you use Authorization headers
             // Note: When allowedOrigins is set to "*", allowCredentials MUST be false or not set.
 }
}




