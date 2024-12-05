package com.CLPayment.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.CLPayment.dto.MP.ItemRecordDTO;
import com.CLPayment.dto.MP.PreferenceRecordDTO;
import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.enums.TransactionType;
import com.CLPayment.model.RequestEntity;
import com.CLPayment.model.TransactionEntity;
import com.CLPayment.service.PreferenceService;
import com.CLPayment.service.RequestService;
import com.CLPayment.service.RequestSignatureService;
import com.CLPayment.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/buychips")
public class PreferenceController {


    @Autowired
    PreferenceService preferenceService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    RequestService requestService;

    @Autowired
    RequestSignatureService requestSignatureService;

    @Value("${MP.webhook}")
    String webhook;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZoneId brTimeZone = ZoneId.of("America/Sao_Paulo");

    @PostMapping
    public ResponseEntity<Mono<Object>> createPreference(@RequestHeader HttpHeaders headers,
							 @RequestBody Map<String, Object> json) {
	ObjectMapper objectMapper = new ObjectMapper();
	String body;
	try {
	    body = objectMapper.writeValueAsString(json);
	} catch (JsonProcessingException e) {
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				 .body(Mono.just("Erro ao converter JSON para String"));
	}
	RequestEntity request = new RequestEntity("/buychips", RequestMethod.POST, body, LocalDateTime.now());

	String xSignature = headers.getFirst("x-signature");
	String xRequestId = headers.getFirst("x-request-id");
	String inventory_id = (String) json.get("inventory_id");

	boolean isValid = requestSignatureService.validateClRequest(xSignature, xRequestId, inventory_id);
	if (!isValid) {
	    request.setHttpStatus(403);
	    requestService.save(request);
	    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}

	LocalDateTime creation_date = LocalDateTime.now();
	LocalDateTime expiration_date = LocalDateTime.now().plusDays(7);

	UUID transaction_id = UUID.randomUUID();

	ItemRecordDTO item = new ItemRecordDTO(transaction_id.toString(),
					       (String) json.get("title"),
					       (Integer) json.get("chips_qty"),
					       "BRL",
					       (Integer) json.get("unit_price"));

	PreferenceRecordDTO preferenceObj = new PreferenceRecordDTO(
								    new ItemRecordDTO[] {
											  item
								    },
								    new PreferenceRecordDTO.BackUrlRecordDTO("https://www.dicio.com.br/sucesso/",
													     "https://www.dicio.com.br/pendente/",
													     "https://www.dicio.com.br/falha/"),
								    webhook + "?source_news=webhooks",
								    inventory_id,
								    creation_date.atZone(brTimeZone).format(formatter),
								    expiration_date.atZone(brTimeZone)
										   .format(formatter));

	Mono<PreferenceRecordDTO> preferenceMono = preferenceService.create(preferenceObj)
								    .flatMap(pr -> {
									TransactionEntity transaction = new TransactionEntity(
															      transaction_id.toString(),
															      TransactionType.BUY_CHIPS,
															      TransactionStatus.pending,
															      inventory_id,
															      item.quantity(),
															      pr.id(),
															      creation_date,
															      creation_date,
															      expiration_date);

									return Mono.fromRunnable(() -> transactionService.save(transaction))
										   .thenReturn(pr);
								    })
								    .onErrorResume(e -> {
									return Mono.just(new PreferenceRecordDTO("Erro ao criar a preferência"));
								    });

	return ResponseEntity.ok(preferenceMono
					       .map(preference -> {
						   if (preference.id() == null) {
						       request.setHttpStatus(500);
						       request.setResponse("Erro ao criar a preferência");
						       requestService.save(request);
						       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
									    .body(preference);
						   }
						   request.setHttpStatus(200);
						   request.setResponse(preference.init_point() + preference.sandbox_init_point());
						   requestService.save(request);
						   return ResponseEntity.ok(preference);
					       }));
    }
}
