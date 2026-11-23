package cl.duoc.huertoapi.dto;

import cl.duoc.huertoapi.model.EstadoSector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// DTO: los datos que viajan en JSON entre la app y el microservicio.
// Tiene los mismos nombres de campos que la entidad Sector de la app (Room).
public record SectorDto(
        Long id,

        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "^S-\\d{2}$", message = "Formato de código: S-01")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
        String nombre,

        @Size(max = 200, message = "Máximo 200 caracteres")
        String descripcion,

        @NotBlank(message = "La ubicación es obligatoria")
        String ubicacion,

        @NotNull(message = "El estado es obligatorio")
        EstadoSector estado,

        @NotNull(message = "El responsable es obligatorio")
        Integer responsableId,

        String fechaCreacion,
        String fechaActualizacion
) {
}
