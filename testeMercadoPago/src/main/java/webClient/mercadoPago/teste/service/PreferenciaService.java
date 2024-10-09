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

    public Mono<PreferenciaRecordDTO> findById(String id) {
	return webClient.get()
			.uri("/{id}", id)
			.headers(h -> h.setBearerAuth(mpAccessToken))
			.retrieve()
			.bodyToMono(PreferenciaRecordDTO.class);
    }

    public Mono<PreferenciaRecordDTO> create(PreferenciaRecordDTO preferenciaRecordDTO) {
	return webClient.post()
			.headers(h -> h.setBearerAuth(mpAccessToken))
			.bodyValue(preferenciaRecordDTO)
			.retrieve()
			.bodyToMono(PreferenciaRecordDTO.class);
    }

    public Mono<Void> update(String id, String data) {
	return findById(id).flatMap(pr -> {
	    PreferenciaRecordDTO preferenciaUpdate = new PreferenciaRecordDTO(pr.items(), pr.back_urls(),
									      pr.notification_url(),
									      pr.external_reference(),
									      pr.expires(),
									      pr.expiration_date_from(),
									      data, pr.id(),
									      pr.init_point(),
									      pr.sandbox_init_point());
	    
	    return webClient.put()
			.uri("/{id}", id)
			.headers(h -> h.setBearerAuth(mpAccessToken))
			.bodyValue(preferenciaUpdate)
			.retrieve()
			.bodyToMono(Void.class);
	});
    }

}
