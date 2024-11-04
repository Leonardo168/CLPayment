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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CLPayment.dto.Efi.PixRecordDTO;
import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.enums.TransactionType;
import com.CLPayment.model.TransactionModel;
import com.CLPayment.service.CLMainService;
import com.CLPayment.service.EfiService;
import com.CLPayment.service.RequestSignatureService;
import com.CLPayment.service.TransactionService;

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
    CLMainService clMainService;
    
    @Autowired
    RequestSignatureService requestSignatureService;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendPix(@RequestBody Map<String, Object> json) {

	String inventory_id = (String) json.get("inventory_id");
	String chave = (String) json.get("chave_pix");
	int chips_qty = (Integer) json.get("chips_qty");
	int unit_price = (Integer) json.get("unit_price");

	LocalDateTime creation_date = LocalDateTime.now();
	LocalDateTime expiration_date = LocalDateTime.now().plusDays(7);

	PixRecordDTO pixRecordDTO = new PixRecordDTO(String.format(Locale.US, "%.2f", (float) chips_qty * unit_price),
						     new PixRecordDTO.PagadorRecordDTO(efi_chave, "Resgate de fichas Common League"),
						     new PixRecordDTO.FavorecidoRecordDTO(chave));

	UUID transaction_id = UUID.randomUUID();

	TransactionModel transactionModel = new TransactionModel(transaction_id.toString(), TransactionType.SELL_CHIPS,
								 TransactionStatus.pending,
								 inventory_id, chips_qty,
								 creation_date, creation_date,
								 expiration_date);
	
	return efiService.sendPix(pixRecordDTO, transaction_id)
                .then(Mono.fromRunnable(() -> transactionService.save(transactionModel)))
                .then(Mono.just(ResponseEntity.ok().build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao enviar PIX"));
    }
    
    @PostMapping("/notification")
    public ResponseEntity<Object> confirmPix(@RequestBody Map<String, Object> json){
	@SuppressWarnings("unchecked")
	List<Map<String, Object>> pixList = (List<Map<String,Object>>) json.get("pix");
	
	if (pixList != null && !pixList.isEmpty()) {
	    Map<String, Object> pix = pixList.get(0);
	    
	    String status = (String) pix.get("status");
	    
	    @SuppressWarnings("unchecked")
	    Map<String, Object> gnExtras = (Map<String, Object>) pix.get("gnExtras");
	    String idEnvio = (String) gnExtras.get("idEnvio");
	    String transaction_id = formatAsUUID(idEnvio);
	    
	    Optional<TransactionModel> transactionOptional = transactionService.findById(transaction_id);
	    if (!transactionOptional.isPresent()) {
		System.out.println("Transação " + transaction_id + " não encontrada");
	    } else {
		TransactionModel transaction = new TransactionModel();
		BeanUtils.copyProperties(transactionOptional.get(), transaction);
		LocalDateTime update_date = LocalDateTime.now();
		transaction.setUpdate_date(update_date);
		transaction.setStatus(TransactionStatus.fromString(status));
		
		transactionService.save(transaction);
		
		if (transaction.getStatus() == TransactionStatus.approved) {
		    String generatedRequestId = UUID.randomUUID().toString();
		    String transaction_idString = transaction_id.toString();
		    String generatedSignature = requestSignatureService.generateSignature(generatedRequestId, transaction_idString);
		    
		    clMainService.process(transaction_idString, generatedSignature, generatedRequestId).subscribe();
		}
	    }
	    
	    return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    public static String formatAsUUID(String string) {
        return String.format(
            Locale.ROOT, "%s-%s-%s-%s-%s",
            string.substring(0, 8),
            string.substring(8, 12),
            string.substring(12, 16),
            string.substring(16, 20),
            string.substring(20, 32)
        );
    }
}

