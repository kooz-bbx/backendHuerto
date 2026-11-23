package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.CultivoDto;
import cl.duoc.huertoapi.exception.RecursoNoEncontradoException;
import cl.duoc.huertoapi.exception.ReglaNegocioException;
import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.model.EstadoCultivo;
import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias del servicio de cultivos (reglas de fechas y sector)
@ExtendWith(MockitoExtension.class)
class CultivoServiceTest {

    @Mock
    private CultivoRepository cultivoRepository;

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private CultivoService cultivoService;

    private CultivoDto cultivo(String siembra, String cosecha, Long sectorId) {
        return new CultivoDto(null, "Lechuga", "Prueba", sectorId, siembra, "", cosecha,
                2, EstadoCultivo.PLANIFICADO, "");
    }

    @Test
    void cosecha_antes_de_la_siembra_lanza_error() {
        CultivoDto malo = cultivo("10/11/2026", "01/11/2026", 1L);

        assertThrows(ReglaNegocioException.class, () -> cultivoService.crear(malo));
        verify(cultivoRepository, never()).save(any());
    }

    @Test
    void cosecha_el_mismo_dia_de_la_siembra_es_valida() {
        assertDoesNotThrow(() -> cultivoService.validarFechas(cultivo("10/11/2026", "10/11/2026", 1L)));
    }

    @Test
    void fecha_que_no_existe_lanza_error() {
        // 31 de febrero no existe: el formato estricto lo rechaza
        assertThrows(ReglaNegocioException.class,
                () -> cultivoService.validarFechas(cultivo("31/02/2026", "10/03/2026", 1L)));
    }

    @Test
    void crear_cultivo_en_sector_inexistente_lanza_404() {
        when(sectorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> cultivoService.crear(cultivo("01/11/2026", "01/12/2026", 99L)));
    }

    @Test
    void crear_cultivo_valido_lo_guarda_con_su_sector() {
        Sector sector = new Sector();
        sector.setId(1L);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(cultivoRepository.save(any(Cultivo.class))).thenAnswer(inv -> {
            Cultivo c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });

        CultivoDto creado = cultivoService.crear(cultivo("01/11/2026", "01/12/2026", 1L));

        assertEquals(7L, creado.id().longValue());
        assertEquals(1L, creado.sectorId().longValue());
    }
}
