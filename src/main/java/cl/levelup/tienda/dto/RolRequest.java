package cl.levelup.tienda.dto;

import cl.levelup.tienda.model.Rol;
import jakarta.validation.constraints.NotNull;

public record RolRequest(@NotNull Rol rol) {
}
