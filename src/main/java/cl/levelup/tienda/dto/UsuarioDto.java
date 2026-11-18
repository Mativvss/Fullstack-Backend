package cl.levelup.tienda.dto;

import cl.levelup.tienda.model.Rol;
import cl.levelup.tienda.model.Usuario;

public record UsuarioDto(Long id, String nombre, String email, Rol rol) {

    public static UsuarioDto de(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
