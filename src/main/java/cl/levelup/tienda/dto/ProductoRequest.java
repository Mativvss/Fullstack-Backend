package cl.levelup.tienda.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
        String nombre,
        @NotBlank(message = "La categoría es obligatoria")
        String categoria,
        @NotNull(message = "El precio es obligatorio")
        @Min(value = 1, message = "El precio debe ser mayor a 0")
        Integer precio,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,
        String img,
        @Size(max = 500, message = "La descripción admite hasta 500 caracteres")
        String descripcion) {
}
