package webClient.mercadoPago.teste.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import webClient.mercadoPago.teste.dto.PagamentoRecordDTO;
import webClient.mercadoPago.teste.service.OrderService;
import webClient.mercadoPago.teste.service.PagamentoService;

@RestController
@RequestMapping("/pagamento")
public class PagamentoController {
    @Autowired
    OrderService orderService;
    
    @Autowired
    PagamentoService pagamentoService;
    
    @PostMapping("/notification")
    @ResponseStatus(HttpStatus.OK)
    public void confirmarPagamento(@RequestBody Map<String, Object> dados) {
	@SuppressWarnings("unchecked")
	Map<String, Object> data = (Map<String, Object>) dados.get("data");
	String idPagamento = (String) data.get("id");
        System.out.println("\n\n"+idPagamento+"\n\n");
        
        Mono<PagamentoRecordDTO> pagamento = pagamentoService.findById(idPagamento);
        pagamento.subscribe(pg -> {
            System.out.println("\n----------------------------------------------------------------------------------------------------------------");
            System.out.println("Id do pedido: "+ pg.additional_info().items().get(0).id());
            System.out.println("Status: "+ pg.status());
            System.out.println("Referência: " + pg.external_reference());
            System.out.println("Valor: " + pg.transaction_amount());
            System.out.println("----------------------------------------------------------------------------------------------------------------\n");
        });
    }

}
