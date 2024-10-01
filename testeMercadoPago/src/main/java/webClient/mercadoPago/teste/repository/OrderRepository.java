package webClient.mercadoPago.teste.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import webClient.mercadoPago.teste.model.OrderModel;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, String>{

}
