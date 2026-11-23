package cl.duoc.huertoapi.config;

import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.model.EstadoCultivo;
import cl.duoc.huertoapi.model.EstadoSector;
import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Carga los mismos datos ficticios que trae la app (DatosPrueba.kt), solo si la tabla está vacía.
// Así los ids coinciden: S-01 es el sector 1 en la app y en MySQL.
@Component
public class DatosIniciales implements CommandLineRunner {

    private final SectorRepository sectorRepository;
    private final CultivoRepository cultivoRepository;

    public DatosIniciales(SectorRepository sectorRepository, CultivoRepository cultivoRepository) {
        this.sectorRepository = sectorRepository;
        this.cultivoRepository = cultivoRepository;
    }

    @Override
    public void run(String... args) {
        if (sectorRepository.count() > 0) return;

        Sector s1 = sectorRepository.save(sector("S-01", "Bancal Norte", "Bancal elevado para hortalizas de hoja",
                "Junto a la sala de 5° básico", EstadoSector.EN_USO, 1, "02/09/2026", "02/09/2026"));
        Sector s2 = sectorRepository.save(sector("S-02", "Invernadero", "Invernadero pequeño para almácigos",
                "Patio trasero", EstadoSector.EN_USO, 2, "02/09/2026", "10/09/2026"));
        Sector s3 = sectorRepository.save(sector("S-03", "Jardín Aromático", "Hierbas aromáticas y medicinales",
                "Entrada del huerto", EstadoSector.EN_MANTENIMIENTO, 1, "05/09/2026", "20/09/2026"));
        sectorRepository.save(sector("S-04", "Compostera", "Zona de compost y lombricultura",
                "Esquina sur", EstadoSector.DISPONIBLE, 2, "05/09/2026", "05/09/2026"));

        cultivoRepository.save(cultivo("Lechuga", "Lechuga costina para ensaladas", s1, "10/09/2026", "25/09/2026",
                "05/11/2026", 2, EstadoCultivo.EN_CRECIMIENTO, "Crece bien, vigilar caracoles"));
        cultivoRepository.save(cultivo("Acelga", "Acelga verde", s1, "12/09/2026", "",
                "20/11/2026", 3, EstadoCultivo.EN_CRECIMIENTO, ""));
        cultivoRepository.save(cultivo("Tomate", "Almácigos de tomate cherry", s2, "01/09/2026", "",
                "15/12/2026", 2, EstadoCultivo.SEMBRADO, ""));
        cultivoRepository.save(cultivo("Albahaca", "Albahaca para el taller de cocina", s3, "15/09/2026", "",
                "30/10/2026", 2, EstadoCultivo.LISTO_PARA_COSECHAR, ""));
        cultivoRepository.save(cultivo("Menta", "Menta y hierbabuena", s3, "05/09/2026", "",
                "25/10/2026", 3, EstadoCultivo.CON_PROBLEMAS, "Hojas amarillas en un sector"));
        cultivoRepository.save(cultivo("Zanahoria", "Zanahoria para 3° básico", s1, "20/10/2026", "",
                "20/01/2027", 4, EstadoCultivo.PLANIFICADO, ""));
    }

    private Sector sector(String codigo, String nombre, String descripcion, String ubicacion,
                          EstadoSector estado, int responsableId, String creado, String actualizado) {
        Sector s = new Sector();
        s.setCodigo(codigo);
        s.setNombre(nombre);
        s.setDescripcion(descripcion);
        s.setUbicacion(ubicacion);
        s.setEstado(estado);
        s.setResponsableId(responsableId);
        s.setFechaCreacion(creado);
        s.setFechaActualizacion(actualizado);
        return s;
    }

    private Cultivo cultivo(String nombre, String descripcion, Sector sector, String siembra, String trasplante,
                            String cosecha, int riego, EstadoCultivo estado, String observaciones) {
        Cultivo c = new Cultivo();
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        c.setSector(sector);
        c.setFechaSiembra(siembra);
        c.setFechaTrasplante(trasplante);
        c.setFechaCosecha(cosecha);
        c.setFrecuenciaRiegoDias(riego);
        c.setEstado(estado);
        c.setObservaciones(observaciones);
        return c;
    }
}
