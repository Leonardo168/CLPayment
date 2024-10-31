package com.CLPayment.controller;

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
import com.CLPayment.service.EfiService;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/withdraw")
public class EfiController {

    @Value("${Efi.chave}")
    private String efi_chave;

    @Autowired
    EfiService efiService;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendPix(@RequestBody Map<String, Object> data) {
	String valor = (String) data.get("valor");
	String chave = (String) data.get("chave_pix");

	PixRecordDTO pixRecordDTO = new PixRecordDTO(valor,
						     new PagadorRecordDTO(efi_chave, "Resgate de fichas Common League"),
						     new FavorecidoRecordDTO(chave));

	UUID transaction_id = UUID.randomUUID();

	return efiService.sendPix(pixRecordDTO, transaction_id)
			 .then(Mono.just(ResponseEntity.ok().build()))
			 .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						      .body("Erro ao enviar PIX"));
    }

}
