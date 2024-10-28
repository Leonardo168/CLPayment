package webClient.mercadoPago.teste.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import webClient.mercadoPago.teste.service.RequestSignatureService;


@RestController
public class RequestSignatureController {
    
    @Autowired
    RequestSignatureService testeAssinaturaService;
    
    @PostMapping("/assinatura")
    public ResponseEntity<String> verificaAssinatura(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> dados) {
	@SuppressWarnings("unchecked")
	Map<String, Object> data = (Map<String, Object>) dados.get("data");
	String dataId = (String) data.get("id");
	
	String xSignature = headers.getFirst("x-signature");
        String xRequestId = headers.getFirst("x-request-id");
	
        boolean isValid = testeAssinaturaService.validateRequest(xSignature, xRequestId, dataId);
        
        if (isValid) {
	    return ResponseEntity.ok("ok");
	} else {
	    return ResponseEntity.status(403).body("inválido");
	}
    }
    
    
}
