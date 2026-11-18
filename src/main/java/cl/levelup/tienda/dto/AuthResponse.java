package cl.levelup.tienda.dto;

public record AuthResponse(String accessToken, String refreshToken, UsuarioDto usuario) {
}
