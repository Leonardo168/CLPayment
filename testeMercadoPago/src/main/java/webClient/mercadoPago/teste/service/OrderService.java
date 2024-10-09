package webClient.mercadoPago.teste.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import webClient.mercadoPago.teste.model.OrderModel;
import webClient.mercadoPago.teste.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;
    
    @Transactional
    public void save(OrderModel order) {
	orderRepository.save(order);
    }
    
    public Optional<OrderModel> findById(String id) {
	return orderRepository.findById(id);
    }
}
