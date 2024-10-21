package webClient.mercadoPago.teste.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import webClient.mercadoPago.teste.model.TransactionModel;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, String> {

}
