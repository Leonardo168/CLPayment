package com.CLPayment.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.model.RequestEntity;
import com.CLPayment.model.TransactionEntity;
import com.CLPayment.service.CLMainService;
import com.CLPayment.service.PaymentService;
import com.CLPayment.service.PreferenceService;
import com.CLPayment.service.RequestService;
import com.CLPayment.service.RequestSignatureService;
import com.CLPayment.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    CLMainService clMainService;

    @Autowired
    RequestSignatureService requestSignatureService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    RequestService requestService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PreferenceService preferenciaService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZoneId brTimeZone = ZoneId.of("America/Sao_Paulo");

    @PostMapping("/notification")
    public Mono<ResponseEntity<Object>> confirmarPagamento(@RequestHeader HttpHeaders headers,
							   @RequestBody Map<String, Object> json) {
	ObjectMapper objectMapper = new ObjectMapper();
	String body;
	try {
	    body = objectMapper.writeValueAsString(json);
	} catch (JsonProcessingException e) {
	    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					   .body(Mono.just("Erro ao converter JSON para String")));
	}
	RequestEntity request = new RequestEntity("/withdraw", RequestMethod.POST, body, LocalDateTime.now());

	@SuppressWarnings("unchecked")
	Map<String, Object> data = (Map<String, Object>) json.get("data");
	String payment_id = (String) data.get("id");
	if (payment_id == null) {
		request.setHttpStatus(400);
		requestService.save(request);
		return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
	    }

	String xSignature = headers.getFirst("x-signature");
	String xRequestId = headers.getFirst("x-request-id");

	boolean isValid = requestSignatureService.validateMpRequest(xSignature, xRequestId, payment_id);

	if (!isValid) {
	    request.setHttpStatus(403);
	    requestService.save(request);
	    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(null));
	}

	return paymentService.findById(payment_id)
			     .flatMap(pg -> {
				 String transaction_id = pg.additional_info().items().get(0).id();

				 Optional<TransactionEntity> transactionOptional = transactionService.findById(transaction_id);
				 if (!transactionOptional.isPresent()) {
				     request.setHttpStatus(404);
				     request.setResponse("Transação não encontrda");
				     requestService.save(request);
				     return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
				 }
				 TransactionEntity transaction = new TransactionEntity();
				 BeanUtils.copyProperties(transactionOptional.get(), transaction);
				 LocalDateTime update_date = LocalDateTime.now();
				 transaction.setPayment_id_mp(payment_id);
				 transaction.setUpdate_date(update_date);
				 transaction.setStatus(TransactionStatus.fromString(pg.status()));

				 transactionService.save(transaction);

				 if (transaction.getStatus() == TransactionStatus.approved) {
				     preferenciaService.update(transaction.getPreference_id_mp(),
							       update_date.atZone(brTimeZone).format(formatter))
						       .subscribe();

				     String generatedRequestId = UUID.randomUUID().toString();
				     String transaction_idString = transaction_id.toString();
				     String generatedSignature = requestSignatureService.generateSignature(generatedRequestId,
													   transaction_idString);

				     clMainService.process(transaction_idString, generatedSignature, generatedRequestId)
						  .subscribe();
				 }
				 request.setHttpStatus(200);
				 requestService.save(request);
				 return Mono.just(ResponseEntity.status(HttpStatus.OK).body(null));
			     })
			     .onErrorResume(e -> {
				 if (e instanceof WebClientResponseException.NotFound) {
				     request.setHttpStatus(404);
				     request.setResponse("Pagamento não encontrado");
				     requestService.save(request);
				     return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
				}
				 request.setHttpStatus(500);
				 requestService.save(request);
				 return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
			     });
    }
}
