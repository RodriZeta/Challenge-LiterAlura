package com.gutendex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        // Aceptar application/json para evitar 406 del API
        for (var c : rt.getMessageConverters()) {
            if (c instanceof MappingJackson2HttpMessageConverter json) {
                json.setSupportedMediaTypes(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
            }
        }
        return rt;
    }
}
