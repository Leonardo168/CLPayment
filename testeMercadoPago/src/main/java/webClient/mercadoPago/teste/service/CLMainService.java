package webClient.mercadoPago.teste.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Service
public class CLMainService {

    @Value("${MP.webhook}")
    String webhook;
    
    private WebClient webClient;

    public CLMainService(WebClient.Builder builder) {
	webClient = builder.build();
    }
    
    @PostConstruct
    public void init() {
        webClient = webClient.mutate().baseUrl(webhook).build();
    }

    public Mono<Void> confirmPurchase(String transaction_id) {
	return webClient.post()
			.uri("/{id}/process", transaction_id)
			.retrieve()
			.bodyToMono(Void.class);
    }
}
