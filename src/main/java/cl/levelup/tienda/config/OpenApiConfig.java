package cl.levelup.tienda.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Level-Up Gamer API")
                        .version("1.0.0")
                        .description("API REST de la tienda Level-Up Gamer: autenticación JWT, productos, pedidos y usuarios"))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA))
                .components(new Components().addSecuritySchemes(ESQUEMA, new SecurityScheme()
                        .name(ESQUEMA)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
