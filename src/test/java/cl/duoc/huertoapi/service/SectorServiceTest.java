package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.SectorDto;
import cl.duoc.huertoapi.exception.RecursoNoEncontradoException;
import cl.duoc.huertoapi.exception.ReglaNegocioException;
import cl.duoc.huertoapi.model.EstadoSector;
import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias del servicio de sectores.
// Usamos Mockito para "simular" los repositorios: no se necesita MySQL para ejecutarlas.
@ExtendWith(MockitoExtension.class)
class SectorServiceTest {

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private CultivoRepository cultivoRepository;

    @InjectMocks
    private SectorService sectorService;

    private SectorDto dtoValido(String codigo) {
        return new SectorDto(null, codigo, "Bancal Sur", "Bancal nuevo", "Patio",
                EstadoSector.DISPONIBLE, 1, "20/11/2026", "20/11/2026");
    }

    @Test
    void crear_sector_valido_lo_guarda_con_codigo_en_mayusculas() {
        when(sectorRepository.existsByCodigoIgnoreCase("S-05")).thenReturn(false);
        when(sectorRepository.save(any(Sector.class))).thenAnswer(inv -> {
            Sector s = inv.getArgument(0);
            s.setId(5L);
            return s;
        });

        SectorDto creado = sectorService.crear(dtoValido("s-05"));

        assertEquals(5L, creado.id().longValue());
        assertEquals("S-05", creado.codigo());
    }

    @Test
    void crear_sector_con_codigo_repetido_lanza_error_y_no_guarda() {
        when(sectorRepository.existsByCodigoIgnoreCase("S-01")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> sectorService.crear(dtoValido("S-01")));
        verify(sectorRepository, never()).save(any());
    }

    @Test
    void eliminar_sector_con_cultivos_no_se_permite() {
        Sector sector = new Sector();
        sector.setId(1L);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(cultivoRepository.countBySectorId(1L)).thenReturn(3L);

        assertThrows(ReglaNegocioException.class, () -> sectorService.eliminar(1L));
        verify(sectorRepository, never()).delete(any());
    }

    @Test
    void buscar_sector_que_no_existe_lanza_404() {
        when(sectorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sectorService.buscar(99L));
    }
}
