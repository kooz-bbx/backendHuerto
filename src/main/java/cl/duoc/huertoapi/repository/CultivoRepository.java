package cl.duoc.huertoapi.repository;

import cl.duoc.huertoapi.model.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    // SELECT * FROM cultivos WHERE sector_id = ? (Spring lo genera por el nombre del método)
    List<Cultivo> findBySectorIdOrderByNombreAsc(Long sectorId);

    long countBySectorId(Long sectorId);
}
