package com.bolsadeideas.springboot.reactor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.models.Comentarios;
import com.bolsadeideas.springboot.reactor.models.Usuario;
import com.bolsadeideas.springboot.reactor.models.UsuarioComentarios;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploContraPresionLimitRate();
	}
	
	public void ejemploContraPresionLimitRate() {
	    //Para el manejo de contra presion cuando el suscriber no puede procesar tantos
		//productors podemos usar el limitrate o bien implementar la interfaz
		//el suscriptor le va a indicar al productor cuantos elementos puede manejar
		// new Subscriber y en sus metodos modificar el comportamiento
		Flux.range(1, 10)
		.log()
		.limitRate(5)
		.subscribe();
	}
	
	public void ejemploContraPresionNewSubscriber() {
	    //Para el manejo de contra presion cuando el suscriber no puede procesar tantos
		//productors podemos usar el limitrate o bien implementar la interfaz
		//el suscriptor le va a indicar al productor cuantos elementos puede manejar
		// new Subscriber y en sus metodos modificar el comportamiento
		Flux.range(1, 10)
		.log()
		.subscribe(new Subscriber<Integer>() {
			private Subscription s;
			private Integer limite = 5;
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
			}

			@Override
			public void onNext(Integer item) {
				log.info(item.toString());
				consumido++;
				if(consumido==limite) {
					consumido = 0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable throwable) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}	
		});
	}
	
	public void ejemploIntervaloDesdeCreate() {
		Flux.create(emitter ->{
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;
				@Override
				public void run() {
					emitter.next(++contador);
					if(contador== 10) {
						timer.cancel();
						emitter.complete();
					}
					if(contador == 5) {
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5"));
					}
				}
			}, 1000, 1000);
		})
		.subscribe(next -> log.info(next.toString()), 
				   error->error.getMessage(),
				   ()-> log.info("Hemos terminado"));
	}
	
	public void ejemploIntervaloInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap(i -> {
			if(i>=5) {
				return Flux.error(new InterruptedException("Solo hasta 5"));
			}
			return Flux.just(i);
		})
		.map(i->"Hola " + i)
		.retry(2)
		.subscribe(s->log.info(s.toString()),e->log.info(e.getMessage()));
	
		latch.await();
	}
	
	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1,12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i-> log.info(i.toString()));
		rango.subscribe();
		
	}
	
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1,12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		//blocklast bloquea hasta la ejecucion del ultimo elemento no es recomendable ya que puede causar cuello de botella
		rango.zipWith(retraso, (ra, re) -> ra)
		.doOnNext(i->log.info(i.toString()))
		.blockLast();
	}
		
	public void ejemploZipWIthRangos() {
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1,2,3,4)
		.map(i -> (i*2))
		.zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
		.subscribe(texto -> log.info(texto));
	}
	
	public void ejemploUsuarioComentarioZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Connors"));
		
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
