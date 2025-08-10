package com.gutendex.client;

import com.gutendex.dto.GutendexResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GutendexClient {

    private final RestTemplate restTemplate;

    @Autowired
    public GutendexClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GutendexResponse buscar(String query) {
        String url = UriComponentsBuilder.fromHttpUrl("https://gutendex.com/books/")
                .queryParam("search", query)
                .build()
                .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.getForObject(url, GutendexResponse.class);
    }
}
