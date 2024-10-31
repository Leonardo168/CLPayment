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
import org.springframework.web.bind.annotation.RestController;

import com.CLPayment.dto.MP.PaymentRecordDTO;
import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.model.TransactionModel;
import com.CLPayment.service.CLMainService;
import com.CLPayment.service.PaymentService;
import com.CLPayment.service.PreferenceService;
import com.CLPayment.service.RequestSignatureService;
import com.CLPayment.service.TransactionService;

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
    PaymentService paymentService;

    @Autowired
    PreferenceService preferenciaService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZoneId brTimeZone = ZoneId.of("America/Sao_Paulo");
    
    @PostMapping("/notification")
    public ResponseEntity<Object> confirmarPagamento(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> dados) {
	@SuppressWarnings("unchecked")
	Map<String, Object> data = (Map<String, Object>) dados.get("data");
	String payment_id = (String) data.get("id");

	String xSignature = headers.getFirst("x-signature");
        String xRequestId = headers.getFirst("x-request-id");
	
        boolean isValid = requestSignatureService.validateMpRequest(xSignature, xRequestId, payment_id);
        
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}
        
	System.out.println("\n----------------------------------------------------------------------------------------------------------------");
	System.out.println("Id do pagamento: " + payment_id);
	System.out.println("----------------------------------------------------------------------------------------------------------------");

	Mono<PaymentRecordDTO> pagamento = paymentService.findById(payment_id);
	pagamento.subscribe(pg -> {
	    String transaction_id = pg.additional_info().items().get(0).id();

	    System.out.println("\n----------------------------------------------------------------------------------------------------------------");
	    System.out.println("Id da transação: " + transaction_id);
	    System.out.println("Status: " + pg.status());
	    System.out.println("Id do inventário: " + pg.external_reference());
	    System.out.println("Valor: " + pg.transaction_amount());
	    System.out.println("----------------------------------------------------------------------------------------------------------------");

	    Optional<TransactionModel> transactionOptional = transactionService.findById(UUID.fromString(transaction_id));
	    if (!transactionOptional.isPresent()) {
		System.out.println("Transação " + transaction_id + " não encontrada");
	    } else {
		TransactionModel transaction = new TransactionModel();
		BeanUtils.copyProperties(transactionOptional.get(), transaction);
		LocalDateTime update_date = LocalDateTime.now();
		transaction.setPayment_id_mp(payment_id);
		transaction.setUpdate_date(update_date);
		transaction.setStatus(TransactionStatus.fromString(pg.status()));
		
		transactionService.save(transaction);

		System.out.println("\n----------------------------------------------------------------------------------------------------------------");
		System.out.println("Pedido " + transaction_id + " atualizado");
		System.out.println("----------------------------------------------------------------------------------------------------------------");

		if (transaction.getStatus() == TransactionStatus.approved) {
		    preferenciaService.update(transaction.getPreference_id_mp(), update_date.atZone(brTimeZone).format(formatter)).subscribe();
		    System.out.println("\n----------------------------------------------------------------------------------------------------------------");
		    System.out.println("Preferência " + transaction.getPreference_id_mp() + " atualizada");
		    System.out.println("----------------------------------------------------------------------------------------------------------------");
		    
		    String generatedRequestId = UUID.randomUUID().toString();
		    String transaction_idString = transaction_id.toString();
		    String generatedSignature = requestSignatureService.generateSignature(generatedRequestId, transaction_idString);
		    
		    clMainService.confirmPurchase(transaction_idString, generatedSignature, generatedRequestId).subscribe();
		}
	    }
	});
	
	return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
