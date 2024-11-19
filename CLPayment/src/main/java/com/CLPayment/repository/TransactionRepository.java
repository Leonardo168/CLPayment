package com.CLPayment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CLPayment.model.TransactionEntity;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

}
