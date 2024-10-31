package com.CLPayment.controller;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CLPayment.dto.Efi.FavorecidoRecordDTO;
import com.CLPayment.dto.Efi.PagadorRecordDTO;
import com.CLPayment.dto.Efi.PixRecordDTO;
import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.enums.TransactionType;
import com.CLPayment.model.TransactionModel;
import com.CLPayment.service.EfiService;
import com.CLPayment.service.TransactionService;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/withdraw")
public class EfiController {

    @Value("${Efi.chave}")
    private String efi_chave;

    @Autowired
    EfiService efiService;

    @Autowired
    TransactionService transactionService;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendPix(@RequestBody Map<String, Object> data) {

	String inventory_id = (String) data.get("inventory_id");
	String chave = (String) data.get("chave_pix");
	int chips_qty = (Integer) data.get("chips_qty");
	int unit_price = (Integer) data.get("unit_price");

	LocalDateTime creation_date = LocalDateTime.now();
	LocalDateTime expiration_date = LocalDateTime.now().plusDays(7);

	PixRecordDTO pixRecordDTO = new PixRecordDTO(String.format(Locale.US, "%.2f", (float) chips_qty * unit_price),
						     new PagadorRecordDTO(efi_chave, "Resgate de fichas Common League"),
						     new FavorecidoRecordDTO(chave));

	UUID transaction_id = UUID.randomUUID();

	TransactionModel transactionModel = new TransactionModel(transaction_id, TransactionType.SELL_CHIPS,
								 TransactionStatus.pending,
								 UUID.fromString(inventory_id), chips_qty,
								 creation_date, creation_date,
								 expiration_date);
	
	return efiService.sendPix(pixRecordDTO, transaction_id)
                .then(Mono.fromRunnable(() -> transactionService.save(transactionModel)))
                .then(Mono.just(ResponseEntity.ok().build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao enviar PIX"));
    }

}
