package com.CLPayment.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CLPayment.model.RequestEntity;
import com.CLPayment.repository.RequestRepository;

import jakarta.transaction.Transactional;

@Service
public class RequestService {

    @Autowired
    RequestRepository requestRepository;
    
    @Transactional
    public void save(RequestEntity request) {
	request.setResponseDate(LocalDateTime.now());
	requestRepository.save(request);
    }
}
