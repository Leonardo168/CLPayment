package com.CLPayment.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class CLMainService {

    private WebClient cLMainService;

    public CLMainService(WebClient cLMainService) {
	this.cLMainService = cLMainService;
    }

    public Mono<Void> confirmPurchase(String transaction_id, String xSignature, String xRequestId) {
	return cLMainService.post()
			    .uri("/{id}/process", transaction_id)
			    .headers(h -> {
				h.set("x-signature", xSignature);
				h.set("x-request-id", xRequestId);
			    })
			    .retrieve()
			    .bodyToMono(Void.class);
    }
}
