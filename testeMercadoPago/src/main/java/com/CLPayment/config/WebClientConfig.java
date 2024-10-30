package com.CLPayment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${MP.webhook}")
    String webhook;

    @Bean
    WebClient paymentWebClient() {
	return WebClient.builder()
			.baseUrl("https://api.mercadopago.com/v1/payments")
			.build();
    }

    @Bean
    WebClient preferenceWebClient() {
	return WebClient.builder()
			.baseUrl("https://api.mercadopago.com/checkout/preferences")
			.build();
    }

    @Bean
    WebClient cLMainService() {
	return WebClient.builder()
			.baseUrl(webhook)
			.build();
    }
}
