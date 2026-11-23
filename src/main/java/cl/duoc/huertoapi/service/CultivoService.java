package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.CultivoDto;
import cl.duoc.huertoapi.exception.RecursoNoEncontradoException;
import cl.duoc.huertoapi.exception.ReglaNegocioException;
import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

// Reglas de negocio de los cultivos
@Service
public class CultivoService {

    // dd/MM/uuuu con validación estricta (no acepta 31/02/2026)
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final CultivoRepository cultivoRepository;
    private final SectorRepository sectorRepository;

    public CultivoService(CultivoRepository cultivoRepository, SectorRepository sectorRepository) {
        this.cultivoRepository = cultivoRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional(readOnly = true)
    public List<CultivoDto> listar() {
        return cultivoRepository.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CultivoDto> listarPorSector(Long sectorId) {
        if (!sectorRepository.existsById(sectorId)) {
            throw new RecursoNoEncontradoException("No existe el sector " + sectorId);
        }
        return cultivoRepository.findBySectorIdOrderByNombreAsc(sectorId).stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public CultivoDto buscar(Long id) {
        return aDto(obtenerEntidad(id));
    }

    @Transactional
    public CultivoDto crear(CultivoDto dto) {
        validarFechas(dto);
        Cultivo cultivo = new Cultivo();
        copiarDatos(dto, cultivo);
        return aDto(cultivoRepository.save(cultivo));
    }

    @Transactional
    public CultivoDto actualizar(Long id, CultivoDto dto) {
        Cultivo cultivo = obtenerEntidad(id);
        validarFechas(dto);
        copiarDatos(dto, cultivo);
        return aDto(cultivoRepository.save(cultivo));
    }

    @Transactional
    public void eliminar(Long id) {
        cultivoRepository.delete(obtenerEntidad(id));
    }

    // Regla: la cosecha tiene que ser igual o posterior a la siembra
    void validarFechas(CultivoDto dto) {
        LocalDate siembra = convertir(dto.fechaSiembra(), "siembra");
        LocalDate cosecha = convertir(dto.fechaCosecha(), "cosecha");
        if (cosecha.isBefore(siembra)) {
            throw new ReglaNegocioException("La cosecha no puede ser antes de la siembra");
        }
        if (dto.fechaTrasplante() != null && !dto.fechaTrasplante().isBlank()) {
            LocalDate trasplante = convertir(dto.fechaTrasplante(), "trasplante");
            if (trasplante.isBefore(siembra)) {
                throw new ReglaNegocioException("El trasplante no puede ser antes de la siembra");
            }
        }
    }

    private LocalDate convertir(String fecha, String nombre) {
        try {
            return LocalDate.parse(fecha.trim(), FORMATO);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ReglaNegocioException("Fecha de " + nombre + " inválida (usa dd/mm/aaaa)");
        }
    }

    private Cultivo obtenerEntidad(Long id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cultivo " + id));
    }

    private void copiarDatos(CultivoDto dto, Cultivo cultivo) {
        Sector sector = sectorRepository.findById(dto.sectorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el sector " + dto.sectorId()));
        cultivo.setNombre(dto.nombre().trim());
        cultivo.setDescripcion(dto.descripcion() == null ? "" : dto.descripcion().trim());
        cultivo.setSector(sector);
        cultivo.setFechaSiembra(dto.fechaSiembra().trim());
        cultivo.setFechaTrasplante(dto.fechaTrasplante() == null ? "" : dto.fechaTrasplante().trim());
        cultivo.setFechaCosecha(dto.fechaCosecha().trim());
        cultivo.setFrecuenciaRiegoDias(dto.frecuenciaRiegoDias());
        cultivo.setEstado(dto.estado());
        cultivo.setObservaciones(dto.observaciones() == null ? "" : dto.observaciones().trim());
    }

    private CultivoDto aDto(Cultivo c) {
        return new CultivoDto(c.getId(), c.getNombre(), c.getDescripcion(), c.getSector().getId(),
                c.getFechaSiembra(), c.getFechaTrasplante(), c.getFechaCosecha(),
                c.getFrecuenciaRiegoDias(), c.getEstado(), c.getObservaciones());
    }
}
