package cl.levelup.tienda.service;

import cl.levelup.tienda.dto.UsuarioDto;
import cl.levelup.tienda.exception.RecursoNoEncontradoException;
import cl.levelup.tienda.exception.SolicitudInvalidaException;
import cl.levelup.tienda.model.Rol;
import cl.levelup.tienda.model.Usuario;
import cl.levelup.tienda.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAll().stream().map(UsuarioDto::de).toList();
    }

    @Transactional
    public UsuarioDto cambiarRol(Long id, Rol rol) {
        Usuario usuario = buscar(id);
        usuario.setRol(rol);
        return UsuarioDto.de(usuarios.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String emailActual) {
        Usuario usuario = buscar(id);
        if (usuario.getEmail().equals(emailActual)) {
            throw new SolicitudInvalidaException("No puedes eliminar tu propia cuenta.");
        }
        usuarios.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }
}
