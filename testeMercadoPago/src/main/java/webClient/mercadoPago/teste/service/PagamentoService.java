package webClient.mercadoPago.teste.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.PagamentoRecordDTO;

@Service
public class PagamentoService {
    @Value("${MP.AccessToken}")
    private String mpAccessToken;
    
    private final WebClient webClient;
    
    public PagamentoService(WebClient.Builder builder) {
	webClient = builder.baseUrl("https://api.mercadopago.com/v1/payments").build();
    }
    
    public Mono<PagamentoRecordDTO> findById(String idPagamento){
	return webClient.get()
		.uri("/{id}", idPagamento)
		.headers(h -> h.setBearerAuth(mpAccessToken))
		.retrieve()
		.bodyToMono(PagamentoRecordDTO.class);
    }

}
