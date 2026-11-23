package cl.duoc.huertoapi.dto;

import cl.duoc.huertoapi.model.EstadoCultivo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// DTO del cultivo. En vez del objeto Sector completo, viaja solo el sectorId (igual que en la app).
public record CultivoDto(
        Long id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
        String nombre,

        @Size(max = 200, message = "Máximo 200 caracteres")
        String descripcion,

        @NotNull(message = "El sector es obligatorio")
        Long sectorId,

        @NotBlank(message = "La fecha de siembra es obligatoria")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Formato de fecha: dd/mm/aaaa")
        String fechaSiembra,

        String fechaTrasplante,

        @NotBlank(message = "La fecha de cosecha es obligatoria")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Formato de fecha: dd/mm/aaaa")
        String fechaCosecha,

        @NotNull(message = "La frecuencia de riego es obligatoria")
        @Min(value = 1, message = "El riego debe ser entre 1 y 30 días")
        @Max(value = 30, message = "El riego debe ser entre 1 y 30 días")
        Integer frecuenciaRiegoDias,

        @NotNull(message = "El estado es obligatorio")
        EstadoCultivo estado,

        String observaciones
) {
}
