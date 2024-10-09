package webClient.mercadoPago.teste.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import webClient.mercadoPago.teste.dto.PagamentoRecordDTO;
import webClient.mercadoPago.teste.model.OrderModel;
import webClient.mercadoPago.teste.service.OrderService;
import webClient.mercadoPago.teste.service.PagamentoService;
import webClient.mercadoPago.teste.service.PreferenciaService;

@RestController
@RequestMapping("/pagamento")
public class PagamentoController {
    @Autowired
    OrderService orderService;

    @Autowired
    PagamentoService pagamentoService;

    @Autowired
    PreferenciaService preferenciaService;

    @PostMapping("/notification")
    @ResponseStatus(HttpStatus.OK)
    public void confirmarPagamento(@RequestBody Map<String, Object> dados) {
	@SuppressWarnings("unchecked")
	Map<String, Object> data = (Map<String, Object>) dados.get("data");
	String idPagamento = (String) data.get("id");

	System.out.println("\n----------------------------------------------------------------------------------------------------------------");
	System.out.println("Id do pagamento: " + idPagamento);
	System.out.println("----------------------------------------------------------------------------------------------------------------");

	Mono<PagamentoRecordDTO> pagamento = pagamentoService.findById(idPagamento);
	pagamento.subscribe(pg -> {
	    String pedidoId = pg.additional_info().items().get(0).id();

	    System.out.println("\n----------------------------------------------------------------------------------------------------------------");
	    System.out.println("Id do pedido: " + pedidoId);
	    System.out.println("Status: " + pg.status());
	    System.out.println("Id do usuário: " + pg.external_reference());
	    System.out.println("Valor: " + pg.transaction_amount());
	    System.out.println("----------------------------------------------------------------------------------------------------------------");

	    Optional<OrderModel> pedidoOptional = orderService.findById(pedidoId);
	    if (!pedidoOptional.isPresent()) {
		System.out.println("Pedido " + pedidoId + " não encontrado");
	    } else {
		OrderModel pedido = new OrderModel();
		BeanUtils.copyProperties(pedidoOptional.get(), pedido);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
		ZoneId fusoBrasilia = ZoneId.of("America/Sao_Paulo");
		String updated_by = ZonedDateTime.now(fusoBrasilia).format(formatter);
		pedido.setUpdated_by(updated_by);
		pedido.setStatus(pg.status());

		orderService.save(pedido);

		System.out.println("\n----------------------------------------------------------------------------------------------------------------");
		System.out.println("Pedido " + pedidoId + " atualizado");
		System.out.println("----------------------------------------------------------------------------------------------------------------");

		if (pedido.getStatus().equals("approved")) {
		    preferenciaService.update(pedido.getId_preferencia_mp(), updated_by).subscribe();
		    System.out.println("\n----------------------------------------------------------------------------------------------------------------");
		    System.out.println("Preferência " + pedido.getId_preferencia_mp() + " atualizada");
		    System.out.println("----------------------------------------------------------------------------------------------------------------");

		}
	    }
	});
    }

}
