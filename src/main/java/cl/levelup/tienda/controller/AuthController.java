package cl.levelup.tienda.controller;

import cl.levelup.tienda.dto.AuthResponse;
import cl.levelup.tienda.dto.LoginRequest;
import cl.levelup.tienda.dto.RefreshRequest;
import cl.levelup.tienda.dto.RegistroRequest;
import cl.levelup.tienda.dto.UsuarioDto;
import cl.levelup.tienda.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro, renovación de token y usuario actual")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y entrega access token y refresh token (JWT)")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/register")
    @Operation(summary = "Registra un cliente nuevo (mayor de 18 años)")
    public ResponseEntity<UsuarioDto> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.registrar(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva el access token usando el refresh token (que también se rota)")
    public AuthResponse renovar(@Valid @RequestBody RefreshRequest request) {
        return auth.renovar(request.refreshToken());
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve el usuario dueño del access token")
    public UsuarioDto me(Authentication autenticacion) {
        return auth.usuarioActual(autenticacion.getName());
    }
}
