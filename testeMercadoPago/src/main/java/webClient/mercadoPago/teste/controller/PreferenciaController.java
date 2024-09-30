package webClient.mercadoPago.teste.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.BackUrlRecordDTO;
import webClient.mercadoPago.teste.dto.ItemRecordDTO;
import webClient.mercadoPago.teste.dto.PayerRecordDTO;
import webClient.mercadoPago.teste.dto.PreferenciaRecordDTO;
import webClient.mercadoPago.teste.dto.identificationRecordDTO;
import webClient.mercadoPago.teste.service.PreferenciaService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/mercadopago")
public class PreferenciaController {

    @Autowired
    PreferenciaService preferenciaService;
    
    @Value("${MP.webhook}")
    String webhook;

    @PostMapping
    public ResponseEntity<Mono<PreferenciaRecordDTO>> postMethodName() {
	String pedidoId = UUID.randomUUID().toString();

	PreferenciaRecordDTO preferenciaObj = new PreferenciaRecordDTO(
								       new ItemRecordDTO[] {
											     new ItemRecordDTO("idTeste0000",
													       "titleTeste",
													       1,
													       "BRL",
													       5)
								       },
								       new PayerRecordDTO(
											  "email@teste.com",
											  new identificationRecordDTO(
														      "CPF",
														      "cpfTeste124")),
								       new BackUrlRecordDTO(
											    "https://www.dicio.com.br/sucesso/",
											    "https://www.dicio.com.br/pendente/",
											    "https://www.dicio.com.br/falha/"),
								       webhook + pedidoId,
								       "");

	Mono<PreferenciaRecordDTO> preferencia = preferenciaService.create(preferenciaObj);
	preferencia.subscribe(p -> {
	    System.out.println("--------------------------------------------------------");
	    System.out.println("sandbox_init_point: " + p.sandbox_init_point());
	    System.out.println("notification_url:" + p.notification_url());
	    System.out.println("--------------------------------------------------------");
	});

	return ResponseEntity.status(HttpStatus.OK).body(preferencia);
    }

}
