package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.dto.CultivoDto;
import cl.duoc.huertoapi.service.CultivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints REST de cultivos: http://localhost:8080/api/cultivos
@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {

    private final CultivoService cultivoService;

    public CultivoController(CultivoService cultivoService) {
        this.cultivoService = cultivoService;
    }

    @GetMapping
    public List<CultivoDto> listar() {
        return cultivoService.listar();
    }

    @GetMapping("/{id}")
    public CultivoDto buscar(@PathVariable Long id) {
        return cultivoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CultivoDto> crear(@Valid @RequestBody CultivoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.crear(dto));
    }

    @PutMapping("/{id}")
    public CultivoDto actualizar(@PathVariable Long id, @Valid @RequestBody CultivoDto dto) {
        return cultivoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cultivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
