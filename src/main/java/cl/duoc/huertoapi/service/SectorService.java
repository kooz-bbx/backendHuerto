package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.SectorDto;
import cl.duoc.huertoapi.exception.RecursoNoEncontradoException;
import cl.duoc.huertoapi.exception.ReglaNegocioException;
import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Capa de servicio: aquí están las reglas de negocio de los sectores.
// El controlador recibe la petición y el servicio decide qué hacer.
@Service
public class SectorService {

    private final SectorRepository sectorRepository;
    private final CultivoRepository cultivoRepository;

    // Inyección por constructor: Spring entrega los repositorios
    public SectorService(SectorRepository sectorRepository, CultivoRepository cultivoRepository) {
        this.sectorRepository = sectorRepository;
        this.cultivoRepository = cultivoRepository;
    }

    @Transactional(readOnly = true)
    public List<SectorDto> listar() {
        return sectorRepository.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public SectorDto buscar(Long id) {
        return aDto(obtenerEntidad(id));
    }

    @Transactional
    public SectorDto crear(SectorDto dto) {
        String codigo = dto.codigo().trim().toUpperCase();
        if (sectorRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un sector con el código " + codigo);
        }
        Sector sector = new Sector();
        copiarDatos(dto, sector);
        return aDto(sectorRepository.save(sector));
    }

    @Transactional
    public SectorDto actualizar(Long id, SectorDto dto) {
        Sector sector = obtenerEntidad(id);
        String codigo = dto.codigo().trim().toUpperCase();
        if (sectorRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("Ya existe otro sector con el código " + codigo);
        }
        copiarDatos(dto, sector);
        return aDto(sectorRepository.save(sector));
    }

    // Regla: no se puede eliminar un sector que todavía tiene cultivos
    @Transactional
    public void eliminar(Long id) {
        Sector sector = obtenerEntidad(id);
        if (cultivoRepository.countBySectorId(id) > 0) {
            throw new ReglaNegocioException("No se puede eliminar: el sector tiene cultivos asociados");
        }
        sectorRepository.delete(sector);
    }

    Sector obtenerEntidad(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el sector " + id));
    }

    private void copiarDatos(SectorDto dto, Sector sector) {
        sector.setCodigo(dto.codigo().trim().toUpperCase());
        sector.setNombre(dto.nombre().trim());
        sector.setDescripcion(dto.descripcion() == null ? "" : dto.descripcion().trim());
        sector.setUbicacion(dto.ubicacion().trim());
        sector.setEstado(dto.estado());
        sector.setResponsableId(dto.responsableId());
        sector.setFechaCreacion(dto.fechaCreacion());
        sector.setFechaActualizacion(dto.fechaActualizacion());
    }

    private SectorDto aDto(Sector s) {
        return new SectorDto(s.getId(), s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getUbicacion(),
                s.getEstado(), s.getResponsableId(), s.getFechaCreacion(), s.getFechaActualizacion());
    }
}
