package com.CLPayment.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.CLPayment.dto.MP.PaymentRecordDTO;

import reactor.core.publisher.Mono;

@Service
public class PaymentService {
    
    private final WebClient paymentWebClient;

    public PaymentService(WebClient paymentWebClient) {
	this.paymentWebClient = paymentWebClient;
    }

    public Mono<PaymentRecordDTO> findById(String payment_id) {
	return paymentWebClient.get()
			       .uri("/{id}", payment_id)
			       .retrieve()
			       .bodyToMono(PaymentRecordDTO.class);
    }

}
