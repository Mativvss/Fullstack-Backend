package cl.levelup.tienda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record PedidoRequest(
        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Valid
        List<ItemRequest> items,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Ingresa un correo válido")
        String email,
        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,
        @NotNull(message = "El tipo de entrega es obligatorio")
        @Pattern(regexp = "domicilio|retiro", message = "La entrega debe ser domicilio o retiro")
        String entrega,
        String direccion,
        String comuna,
        @NotNull(message = "Debes indicar el resultado del pago")
        Boolean pagoExitoso) {

    public record ItemRequest(
            @NotNull(message = "El producto es obligatorio") Long productoId,
            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad) {
    }
}
