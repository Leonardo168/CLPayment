package com.CLPayment.service;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class EfiService {

    private final WebClient efiWebClient;

    public EfiService(WebClient efiWebClient) {
	this.efiWebClient = efiWebClient;
    }

    public Mono<Map<String, Object>> authorization() {
	return efiWebClient.post()
			   .uri("/oauth/token")
			   .header(HttpHeaders.CONTENT_TYPE, "application/json")
			   .bodyValue("{\"grant_type\": \"client_credentials\"}")
			   .retrieve()
			   .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			   });
    }
}
