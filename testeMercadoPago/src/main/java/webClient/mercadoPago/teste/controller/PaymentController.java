package webClient.mercadoPago.teste.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.PaymentRecordDTO;
import webClient.mercadoPago.teste.enums.TransactionStatus;
import webClient.mercadoPago.teste.model.TransactionModel;
import webClient.mercadoPago.teste.service.CLMainService;
import webClient.mercadoPago.teste.service.PaymentService;
import webClient.mercadoPago.teste.service.PreferenceService;
import webClient.mercadoPago.teste.service.TransactionService;

@RestController
@RequestMapping("/pagamento")
public class PaymentController {
    @Autowired
    CLMainService clMainService;
    
    @Autowired
    TransactionService transactionService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PreferenceService preferenciaService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZoneId brTimeZone = ZoneId.of("America/Sao_Paulo");
    
    @PostMapping("/notification")
    @ResponseStatus(HttpStatus.OK)
    public void confirmarPagamento(@RequestBody Map<String, Object> dados) {
	@SuppressWarnings("unchecked")
	Map<String, Object> data = (Map<String, Object>) dados.get("data");
	String payment_id = (String) data.get("id");

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

	    Optional<TransactionModel> transactionOptional = transactionService.findById(transaction_id);
	    if (!transactionOptional.isPresent()) {
		System.out.println("Transação " + transaction_id + " não encontrada");
	    } else {
		TransactionModel transaction = new TransactionModel();
		BeanUtils.copyProperties(transactionOptional.get(), transaction);
		LocalDateTime updated_by = LocalDateTime.now();
		transaction.setUpdated_by(updated_by);
		transaction.setStatus(TransactionStatus.fromString(pg.status()));
		
		transactionService.save(transaction);

		System.out.println("\n----------------------------------------------------------------------------------------------------------------");
		System.out.println("Pedido " + transaction_id + " atualizado");
		System.out.println("----------------------------------------------------------------------------------------------------------------");

		if (transaction.getStatus() == TransactionStatus.approved) {
		    preferenciaService.update(transaction.getPreference_id_mp(), updated_by.atZone(brTimeZone).format(formatter)).subscribe();
		    System.out.println("\n----------------------------------------------------------------------------------------------------------------");
		    System.out.println("Preferência " + transaction.getPreference_id_mp() + " atualizada");
		    System.out.println("----------------------------------------------------------------------------------------------------------------");
		    
		    clMainService.confirmPurchase(transaction_id).subscribe();
		}
	    }
	});
    }

}
