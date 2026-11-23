package cl.duoc.huertoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio. Al ejecutarlo levanta un servidor en http://localhost:8080
@SpringBootApplication
public class HuertoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuertoApiApplication.class, args);
    }
}
