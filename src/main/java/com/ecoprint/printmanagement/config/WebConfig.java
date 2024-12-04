package com.ecoprint.printmanagement.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Map "/uploads/**" to the "uploads/" folder on the filesystem
		registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Enable CORS for frontend on localhost:3000 (adjust for your actual frontend
		// URL)
		registry.addMapping("/**") // Allow all endpoints
				.allowedOrigins("http://localhost:5173") // Replace with your frontend's origin
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
				.allowedHeaders("*") // Allow all headers
				.allowCredentials(true); // Allow credentials (e.g., cookies, Authorization headers)
	}
	
	@Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Ensure Jackson converters are present
        converters.add(new MappingJackson2HttpMessageConverter());
    }
}
