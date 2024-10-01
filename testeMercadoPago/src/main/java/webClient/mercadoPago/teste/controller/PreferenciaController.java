package webClient.mercadoPago.teste.controller;


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
import webClient.mercadoPago.teste.model.OrderModel;
import webClient.mercadoPago.teste.service.OrderService;
import webClient.mercadoPago.teste.service.PreferenciaService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/mercadopago")
public class PreferenciaController {

    @Autowired
    PreferenciaService preferenciaService;
    
    @Autowired
    OrderService orderService;

    @Value("${MP.webhook}")
    String webhook;

    @PostMapping
    public ResponseEntity<Mono<PreferenciaRecordDTO>> postMethodName() {
	PayerRecordDTO payer = new PayerRecordDTO("email@teste.com",
						  new identificationRecordDTO("CPF", "cpfTeste124"));
	

	PreferenciaRecordDTO preferenciaObj = new PreferenciaRecordDTO(
								       new ItemRecordDTO[] {
											     new ItemRecordDTO("idTeste0000",
													       "titleTeste",
													       1,
													       "BRL",
													       5)
								       },
								       payer,
								       new BackUrlRecordDTO("https://www.dicio.com.br/sucesso/",
											    "https://www.dicio.com.br/pendente/",
											    "https://www.dicio.com.br/falha/"),
								       webhook  + "?source_news=webhooks");

	Mono<PreferenciaRecordDTO> preferencia = preferenciaService.create(preferenciaObj);
	preferencia.subscribe(p -> {
	    OrderModel pedido = new OrderModel(p.id(), payer.identification().type(), payer.identification().number());
	    orderService.save(pedido);

	    System.out.println("--------------------------------------------------------");
	    System.out.println("ID do pedido: " + pedido.getId());
	    System.out.println("URL para notificação: " + webhook);
	    System.out.println("sandbox_init_point: " + p.sandbox_init_point());
	    System.out.println("--------------------------------------------------------");
	});

	return ResponseEntity.status(HttpStatus.OK).body(preferencia);
    }

}
