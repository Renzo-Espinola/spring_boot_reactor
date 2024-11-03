package com.bolsadeideas.springboot.reactor.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.bolsadeideas.springboot.reactor.configuration.RabbitConfig;
import com.bolsadeideas.springboot.reactor.models.Solicitud;

import reactor.core.publisher.Mono;

@Service
public class OrderService {
	private final RabbitTemplate rabbitTemplate;

    public OrderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Mono<Void> placeOrder(Solicitud solicitud) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ORDER_QUEUE, solicitud);
            System.out.println("Pedido enviado a la cola: " + solicitud);
        });
    }
}
