package com.CLPayment.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CLPayment.model.TransactionModel;
import com.CLPayment.repository.TransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;
    
    @Transactional
    public void save(TransactionModel transaction) {
	transactionRepository.save(transaction);
    }
    
    public Optional<TransactionModel> findById(String transaction_id) {
	return transactionRepository.findById(transaction_id);
    }
}
