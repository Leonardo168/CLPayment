package webClient.mercadoPago.teste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import webClient.mercadoPago.teste.service.RequestSignatureService;


@RestController
public class RequestSignatureController {
    
    @Autowired
    RequestSignatureService requestSignatureService;
    
    @PostMapping("/{id}/process")
    public ResponseEntity<String> verificaAssinatura(@RequestHeader HttpHeaders headers, @PathVariable String id) {
	
	String xSignature = headers.getFirst("x-signature");
        String xRequestId = headers.getFirst("x-request-id");
	
        boolean isValid = requestSignatureService.validateClRequest(xSignature, xRequestId, id);
        
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	} 
        
        // Processo do endpoint...
        
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    
    
}
