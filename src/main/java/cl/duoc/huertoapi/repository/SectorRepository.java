package cl.duoc.huertoapi.repository;

import cl.duoc.huertoapi.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA crea solo las consultas: findAll, findById, save, deleteById...
public interface SectorRepository extends JpaRepository<Sector, Long> {

    // ¿Existe otro sector con este código? (para no repetir S-01)
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByCodigoIgnoreCase(String codigo);
}
