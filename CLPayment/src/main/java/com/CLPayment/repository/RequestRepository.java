package com.CLPayment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CLPayment.model.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, String> {

}
