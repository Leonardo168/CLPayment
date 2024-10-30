package com.CLPayment.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.CLPayment.dto.PreferenceRecordDTO;

import reactor.core.publisher.Mono;

@Service
public class PreferenceService {

    private final WebClient preferenceWebClient;

    public PreferenceService(WebClient preferenceWebClient) {
	this.preferenceWebClient = preferenceWebClient;
    }

    public Mono<PreferenceRecordDTO> findById(String id) {
	return preferenceWebClient.get()
				  .uri("/{id}", id)
				  .retrieve()
				  .bodyToMono(PreferenceRecordDTO.class);
    }

    public Mono<PreferenceRecordDTO> create(PreferenceRecordDTO preferenceRecordDTO) {
	return preferenceWebClient.post()
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

	    return preferenceWebClient.put()
				      .uri("/{id}", id)
				      .bodyValue(preferenceUpdate)
				      .retrieve()
				      .bodyToMono(Void.class);
	});
    }

}
