package cl.levelup.tienda.dto;

import cl.levelup.tienda.model.Producto;

public record ProductoDto(Long id, String categoria, String nombre, int precio, int stock, String img,
                          String descripcion) {

    public static ProductoDto de(Producto p) {
        return new ProductoDto(p.getId(), p.getCategoria(), p.getNombre(), p.getPrecio(), p.getStock(),
                p.getImg(), p.getDescripcion());
    }
}
