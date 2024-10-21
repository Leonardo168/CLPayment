package webClient.mercadoPago.teste.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import webClient.mercadoPago.teste.model.TransactionModel;
import webClient.mercadoPago.teste.repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;
    
    @Transactional
    public void save(TransactionModel transaction) {
	transactionRepository.save(transaction);
    }
    
    public Optional<TransactionModel> findById(String id) {
	return transactionRepository.findById(id);
    }
}
