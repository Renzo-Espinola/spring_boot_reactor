package com.bolsadeideas.springboot.reactor.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.bolsadeideas.springboot.reactor.configuration.RabbitConfig;
import com.bolsadeideas.springboot.reactor.models.Solicitud;

@Service
public class NotificationService {
	private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void receiveOrder(Solicitud solicitud) {
        System.out.println("Pedido recibido: " + solicitud);
        sendEmail(solicitud);
    }

    private void sendEmail(Solicitud solicitud) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(solicitud.getEmail());
        message.setSubject("Confirmación de Pedido");
        message.setText("Tu pedido con ID: " + solicitud.getId() + " ha sido recibido. Producto: " + solicitud.getProducto());
        mailSender.send(message);
        System.out.println("Correo enviado a: " + solicitud.getEmail());
    }

}
