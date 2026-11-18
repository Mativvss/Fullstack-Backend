package cl.levelup.tienda.controller;

import cl.levelup.tienda.dto.RolRequest;
import cl.levelup.tienda.dto.UsuarioDto;
import cl.levelup.tienda.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Usuarios", description = "Gestión de usuarios y roles (solo administrador)")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    @Operation(summary = "Lista los usuarios (sin contraseña)")
    public List<UsuarioDto> listar() {
        return usuarios.listar();
    }

    @PutMapping("/{id}/rol")
    @Operation(summary = "Cambia el rol de un usuario")
    public UsuarioDto cambiarRol(@PathVariable Long id, @Valid @RequestBody RolRequest request) {
        return usuarios.cambiarRol(id, request.rol());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un usuario (no puedes eliminarte a ti mismo)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        usuarios.eliminar(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
