package com.CLPayment.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
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

import com.CLPayment.dto.Efi.PixRecordDTO;
import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.enums.TransactionType;
import com.CLPayment.model.RequestEntity;
import com.CLPayment.model.TransactionEntity;
import com.CLPayment.service.CLMainService;
import com.CLPayment.service.EfiService;
import com.CLPayment.service.RequestService;
import com.CLPayment.service.RequestSignatureService;
import com.CLPayment.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/withdraw")
public class EfiController {

    @Value("${Efi.chave}")
    private String efi_chave;

    @Autowired
    EfiService efiService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    RequestService requestService;

    @Autowired
    CLMainService clMainService;

    @Autowired
    RequestSignatureService requestSignatureService;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendPix(@RequestHeader HttpHeaders headers,
						@RequestBody Map<String, Object> json) {
	ObjectMapper objectMapper = new ObjectMapper();
	String body;
	try {
	    body = objectMapper.writeValueAsString(json);
	} catch (JsonProcessingException e) {
	    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					   .body("Erro ao converter JSON para String"));
	}
	RequestEntity request = new RequestEntity("/withdraw", RequestMethod.POST, body, LocalDateTime.now());

	String xSignature = headers.getFirst("x-signature");
	String xRequestId = headers.getFirst("x-request-id");
	String inventory_id = (String) json.get("inventory_id");

	boolean isValid = requestSignatureService.validateClRequest(xSignature, xRequestId, inventory_id);
	if (!isValid) {
	    request.setHttpStatus(403);
	    requestService.save(request);
	    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(null));
	}

	String chave = (String) json.get("chave_pix");
	int chips_qty = (Integer) json.get("chips_qty");
	int unit_price = (Integer) json.get("unit_price");

	LocalDateTime creation_date = LocalDateTime.now();
	LocalDateTime expiration_date = LocalDateTime.now().plusDays(7);

	PixRecordDTO pixRecordDTO = new PixRecordDTO(String.format(Locale.US, "%.2f", (float) chips_qty * unit_price),
						     new PixRecordDTO.PagadorRecordDTO(efi_chave,
										       "Resgate de fichas Common League"),
						     new PixRecordDTO.FavorecidoRecordDTO(chave));

	UUID transaction_id = UUID.randomUUID();

	TransactionEntity transactionEntity = new TransactionEntity(transaction_id.toString(),
								    TransactionType.SELL_CHIPS,
								    TransactionStatus.pending,
								    inventory_id, chips_qty,
								    creation_date, creation_date,
								    expiration_date);

	return efiService.sendPix(pixRecordDTO, transaction_id)
			 .doOnSuccess(response -> {
			     transactionService.save(transactionEntity);
			     request.setHttpStatus(200);
			     requestService.save(request);
			 })
			 .then(Mono.just(ResponseEntity.ok().build()))
			 .onErrorResume(e -> {
			     String response = "Erro ao enviar PIX";
			     request.setHttpStatus(500);
			     request.setResponse(response);
			     requestService.save(request);
			     return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							    .body(response));
			 });
    }

    @PostMapping("/notification")
    public ResponseEntity<Object> confirmPix(@RequestBody Map<String, Object> json) {
	ObjectMapper objectMapper = new ObjectMapper();
	String body;
	try {
	    body = objectMapper.writeValueAsString(json);
	} catch (JsonProcessingException e) {
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				 .body("Erro ao converter JSON para String");
	}
	RequestEntity request = new RequestEntity("/withdraw/notification", RequestMethod.POST, body, LocalDateTime.now());

	@SuppressWarnings("unchecked")
	List<Map<String, Object>> pixList = (List<Map<String, Object>>) json.get("pix");

	if (pixList != null && !pixList.isEmpty()) {
	    Map<String, Object> pix = pixList.get(0);

	    String status = (String) pix.get("status");
	    if (status == null) {
		request.setHttpStatus(400);
		requestService.save(request);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }

	    @SuppressWarnings("unchecked")
	    Map<String, Object> gnExtras = (Map<String, Object>) pix.get("gnExtras");
	    String idEnvio = (String) gnExtras.get("idEnvio");
	    if (idEnvio == null) {
		request.setHttpStatus(400);
		requestService.save(request);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	    String transaction_id = formatAsUUID(idEnvio);

	    Optional<TransactionEntity> transactionOptional = transactionService.findById(transaction_id);
	    if (!transactionOptional.isPresent()) {
		request.setHttpStatus(404);
		requestService.save(request);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    } else {
		TransactionEntity transaction = new TransactionEntity();
		BeanUtils.copyProperties(transactionOptional.get(), transaction);
		LocalDateTime update_date = LocalDateTime.now();
		transaction.setUpdate_date(update_date);
		transaction.setStatus(TransactionStatus.fromString(status));

		transactionService.save(transaction);

		if (transaction.getStatus() == TransactionStatus.approved) {
		    String generatedRequestId = UUID.randomUUID().toString();
		    String transaction_idString = transaction_id.toString();
		    String generatedSignature = requestSignatureService.generateSignature(generatedRequestId,
											  transaction_idString);

		    clMainService.process(transaction_idString, generatedSignature, generatedRequestId).subscribe();
		}
	    }
	    request.setHttpStatus(200);
	    requestService.save(request);
	    return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	request.setHttpStatus(400);
	requestService.save(request);
	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    public static String formatAsUUID(String string) {
	return String.format(
			     Locale.ROOT, "%s-%s-%s-%s-%s",
			     string.substring(0, 8),
			     string.substring(8, 12),
			     string.substring(12, 16),
			     string.substring(16, 20),
			     string.substring(20, 32));
    }
}
