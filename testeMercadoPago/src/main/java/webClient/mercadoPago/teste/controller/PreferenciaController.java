package webClient.mercadoPago.teste.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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
import webClient.mercadoPago.teste.dto.PreferenciaRecordDTO;
import webClient.mercadoPago.teste.model.OrderModel;
import webClient.mercadoPago.teste.service.OrderService;
import webClient.mercadoPago.teste.service.PreferenciaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/preferencia")
public class PreferenciaController {

    @Autowired
    PreferenciaService preferenciaService;

    @Autowired
    OrderService orderService;

    @Value("${MP.webhook}")
    String webhook;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    ZoneId fusoBrasilia = ZoneId.of("America/Sao_Paulo");

    @PostMapping
    public ResponseEntity<Mono<PreferenciaRecordDTO>> criarPedido(@RequestBody Map<String, Object> dados) {

	String created_by = ZonedDateTime.now(fusoBrasilia).format(formatter);
	String expiration_date = ZonedDateTime.now(fusoBrasilia).plusDays(7).format(formatter);

	String userId = (String) dados.get("user_id");
	String fichaId = (String) dados.get("id_ficha");

	ItemRecordDTO item = new ItemRecordDTO(UUID.randomUUID().toString(),
					       (String) dados.get("title"),
					       (Integer) dados.get("qtde_ficha"),
					       "BRL",
					       (Integer) dados.get("unit_price"));

	PreferenciaRecordDTO preferenciaObj = new PreferenciaRecordDTO(
								       new ItemRecordDTO[] {
											     item
								       },
								       new BackUrlRecordDTO("https://www.dicio.com.br/sucesso/",
											    "https://www.dicio.com.br/pendente/",
											    "https://www.dicio.com.br/falha/"),
								       webhook + "?source_news=webhooks",
								       userId, created_by, expiration_date);

	Mono<PreferenciaRecordDTO> preferencia = preferenciaService.create(preferenciaObj);
	preferencia.subscribe(pr -> {
	    OrderModel pedido = new OrderModel(item.id(), pr.id(), userId, fichaId, item.quantity(),
					       (item.quantity() * item.unit_price()), created_by, created_by,
					       expiration_date, "pendente");
	    orderService.save(pedido);

	    System.out.println("\n----------------------------------------------------------------------------------------------------------------");
	    System.out.println("ID do pedido: " + pedido.getId());
	    System.out.println("URL para notificação: " + webhook);
	    System.out.println("init_point: " + pr.init_point());
	    System.out.println("sandbox_init_point: " + pr.sandbox_init_point());
	    System.out.println("----------------------------------------------------------------------------------------------------------------\n");
	});

	return ResponseEntity.status(HttpStatus.OK).body(preferencia);
    }
}
