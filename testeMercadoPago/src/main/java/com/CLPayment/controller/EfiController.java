package com.CLPayment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CLPayment.service.EfiService;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/withdraw")
public class EfiController {
    @Autowired
    EfiService efiService;

    @PostMapping
    public Mono<ResponseEntity<String>> postMethodName() {
	return efiService.authorization()
			 .map(responseMap -> {
			     String accessToken = (String) responseMap.get("access_token");
			     return ResponseEntity.status(HttpStatus.OK).body(accessToken);
			 }).defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    @PutMapping
    public String putMethodName(@PathVariable String id, @RequestBody String entity) {
        //TODO: process PUT request
        
        return entity;
    }

}
