package com.CLPayment.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.CLPayment.dto.Efi.PixRecordDTO;

import reactor.core.publisher.Mono;

@Service
public class EfiService {

    @Value("${Efi.username}")
    private String username;

    @Value("${Efi.password}")
    private String password;

    private final WebClient efiWebClient;

    public EfiService(WebClient efiWebClient) {
	this.efiWebClient = efiWebClient;
    }

    public Mono<String> authorization() {
	return efiWebClient.post()
			   .uri("/oauth/token")
			   .headers(h -> {
			       h.setBasicAuth(username, password);
			       h.set(HttpHeaders.CONTENT_TYPE, "application/json");
			   })
			   .bodyValue("{\"grant_type\": \"client_credentials\"}")
			   .retrieve()
			   .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			   })
			   .map(response -> {
			       String token = (String) response.get("access_token");
			       return token;
			   });
    }

    public Mono<Void> sendPix(PixRecordDTO pixRecordDTO, UUID transaction_id) {
	return authorization()
			      .flatMap(token -> efiWebClient.put()
							    .uri("/v2/gn/pix/{id}", transaction_id.toString().replace("-", ""))
							    .headers(h -> h.setBearerAuth(token))
							    .bodyValue(pixRecordDTO)
							    .retrieve()
							    .bodyToMono(Void.class));
    }
}
