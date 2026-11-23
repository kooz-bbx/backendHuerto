package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.dto.CultivoDto;
import cl.duoc.huertoapi.dto.SectorDto;
import cl.duoc.huertoapi.service.CultivoService;
import cl.duoc.huertoapi.service.SectorService;
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

// Endpoints REST de sectores: http://localhost:8080/api/sectores
@RestController
@RequestMapping("/api/sectores")
public class SectorController {

    private final SectorService sectorService;
    private final CultivoService cultivoService;

    public SectorController(SectorService sectorService, CultivoService cultivoService) {
        this.sectorService = sectorService;
        this.cultivoService = cultivoService;
    }

    @GetMapping                                   // GET /api/sectores
    public List<SectorDto> listar() {
        return sectorService.listar();
    }

    @GetMapping("/{id}")                          // GET /api/sectores/1
    public SectorDto buscar(@PathVariable Long id) {
        return sectorService.buscar(id);
    }

    @GetMapping("/{id}/cultivos")                 // GET /api/sectores/1/cultivos
    public List<CultivoDto> cultivosDelSector(@PathVariable Long id) {
        return cultivoService.listarPorSector(id);
    }

    @PostMapping                                  // POST /api/sectores  (JSON en el cuerpo)
    public ResponseEntity<SectorDto> crear(@Valid @RequestBody SectorDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectorService.crear(dto));
    }

    @PutMapping("/{id}")                          // PUT /api/sectores/1
    public SectorDto actualizar(@PathVariable Long id, @Valid @RequestBody SectorDto dto) {
        return sectorService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")                       // DELETE /api/sectores/1
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sectorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
