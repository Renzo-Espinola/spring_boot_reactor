package com.bolsadeideas.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import models.Comentarios;
import models.Usuario;
import models.UsuarioComentarios;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploZipWIthRangos();
	}
		
	public void ejemploZipWIthRangos() {
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1,2,3,4)
		.map(i -> (i*2))
		.zipWith(rangos, (uno, dos) -> String.format("Primer Flix: %d, Segundo Flux: %d", uno, dos))
		.subscribe(texto -> log.info(texto));
	}
	
	public void ejemploUsuarioComentarioZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Connors"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola pepe, que tal!");
			comentarios.addComentarios("Mañana voy a la playa");
			comentarios.addComentarios("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u,c);
				});
		usuarioComentarios.subscribe(uc-> log.info(uc.toString()));
	}
	
	public void ejemploUsuarioComentarioZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Connors"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola pepe, que tal!");
			comentarios.addComentarios("Mañana voy a la playa");
			comentarios.addComentarios("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios = usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));
		
		usuarioComentarios.subscribe(uc-> log.info(uc.toString()));
	}
	
	
	public void ejemploUsuarioComentarioFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Connors"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola pepe, que tal!");
			comentarios.addComentarios("Mañana voy a la playa");
			comentarios.addComentarios("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u,c)))
		.subscribe(uc-> log.info(uc.toString()));
	}
	
	public void ejemploCollectToList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Renzo", "Espinola"));
		usuariosList.add(new Usuario("Tatiana", "Rios"));
		usuariosList.add(new Usuario("Chucky", "Espinola"));
		usuariosList.add(new Usuario("Athena", "Espinola"));
		usuariosList.add(new Usuario("Michu", "Espinola"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList).collectList().subscribe(lista -> {
			lista.forEach(item -> log.info(item.toString()));
		});

	}
	
	public void ejemploToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Renzo", "Espinola"));
		usuariosList.add(new Usuario("Tatiana", "Rios"));
		usuariosList.add(new Usuario("Chucky", "Espinola"));
		usuariosList.add(new Usuario("Athena", "Espinola"));
		usuariosList.add(new Usuario("Michu", "Espinola"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));


		Flux.fromIterable(usuariosList)
		        .map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {return nombre.toLowerCase();})
				.subscribe(u -> log.info(u.toString()));

	}
	
	public void ejemploFlagMap() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Renzo Espinola");
		usuariosList.add("Tatiana Rios");
		usuariosList.add("Chucky Espinola");
		usuariosList.add("Athena Espinola");
		usuariosList.add("Michu Espinola");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");


		Flux.fromIterable(usuariosList)
		        .map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if("bruce".equalsIgnoreCase(usuario.getNombre())) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					usuario.setNombre(usuario.getNombre().toLowerCase());
					return usuario;
				})
				.subscribe(u -> log.info(u.toString()));

	}
	
	public void ejemploIterable() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Renzo Espinola");
		usuariosList.add("Tatiana Rios");
		usuariosList.add("Chucky Espinola");
		usuariosList.add("Athena Espinola");
		usuariosList.add("Michu Espinola");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		// Flux<String> nombres = Flux.just("Renzo Espinola", "Tatiana Rios", "Chucky
		// Espinola", "Athena Espinola", "Michu Espinola", "Bruce Lee", "Bruce Willis");
		Flux<String> nombres = Flux.fromIterable(usuariosList);

		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> "Bruce".equalsIgnoreCase(usuario.getNombre())).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					usuario.setNombre(usuario.getNombre().toLowerCase());
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(((Throwable) error).getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha Finalizado la ejecucion del observable con exito!");

					}
				});

	}

}
