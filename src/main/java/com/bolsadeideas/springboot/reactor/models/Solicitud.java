package com.bolsadeideas.springboot.reactor.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Solicitud implements Serializable {
    private static final long serialVersionUID = 1L;
	private String id;
	private String email;
	private String producto;


	public Solicitud(String id, String email, String producto) {
		this.id = id;
		this.email = email;
		this.producto = producto;
	}

}
