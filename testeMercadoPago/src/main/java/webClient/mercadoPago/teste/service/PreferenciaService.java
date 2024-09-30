package webClient.mercadoPago.teste.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.PreferenciaRecordDTO;

@Service
public class PreferenciaService {

    @Value("${MP.AccessToken}")
    private String mpAccessToken;
    
    private final WebClient webClient;

    public PreferenciaService(WebClient.Builder builder) {
	this.webClient = builder.baseUrl("https://api.mercadopago.com/checkout/preferences").build();
    }

    public Mono<PreferenciaRecordDTO> create(PreferenciaRecordDTO preferenciaRecordDTO) {
	return webClient.post()
			.header("Authorization", mpAccessToken)
			.bodyValue(preferenciaRecordDTO)
			.retrieve()
			.bodyToMono(PreferenciaRecordDTO.class);
    }

}
