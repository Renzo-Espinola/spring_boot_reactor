package com.bolsadeideas.springboot.reactor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bolsadeideas.springboot.reactor.models.Solicitud;
import com.bolsadeideas.springboot.reactor.service.OrderService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Mono<Void> placeOrder(@RequestBody Solicitud solicitud) {
        return orderService.placeOrder(solicitud);
    }
    
    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("Test OK");
    }

}
