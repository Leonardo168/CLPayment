package webClient.mercadoPago.teste.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.PreferenceRecordDTO;

@Service
public class PreferenceService {

    @Value("${MP.AccessToken}")
    private String mpAccessToken;

    private final WebClient webClient;

    public PreferenceService(WebClient.Builder builder) {
	this.webClient = builder.baseUrl("https://api.mercadopago.com/checkout/preferences").build();
    }

    public Mono<PreferenceRecordDTO> findById(String id) {
	return webClient.get()
			.uri("/{id}", id)
			.headers(h -> h.setBearerAuth(mpAccessToken))
			.retrieve()
			.bodyToMono(PreferenceRecordDTO.class);
    }

    public Mono<PreferenceRecordDTO> create(PreferenceRecordDTO preferenceRecordDTO) {
	return webClient.post()
			.headers(h -> h.setBearerAuth(mpAccessToken))
			.bodyValue(preferenceRecordDTO)
			.retrieve()
			.bodyToMono(PreferenceRecordDTO.class);
    }

    public Mono<Void> update(String id, String date) {
	return findById(id).flatMap(pr -> {
	    PreferenceRecordDTO preferenceUpdate = new PreferenceRecordDTO(pr.items(), pr.back_urls(),
									      pr.notification_url(),
									      pr.external_reference(),
									      pr.expires(),
									      pr.expiration_date_from(),
									      date, pr.id(),
									      pr.init_point(),
									      pr.sandbox_init_point());
	    
	    return webClient.put()
			.uri("/{id}", id)
			.headers(h -> h.setBearerAuth(mpAccessToken))
			.bodyValue(preferenceUpdate)
			.retrieve()
			.bodyToMono(Void.class);
	});
    }

}
