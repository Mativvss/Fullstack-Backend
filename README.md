# Level-Up Gamer — Backend (Spring Boot)

Backend de la tienda Level-Up Gamer, hecho para la Evaluación Parcial 3 de DSY1104 Desarrollo Full Stack II. Expone una API REST con Spring Boot, guarda los datos en MySQL con JPA, autentica con **JWT** (access token + refresh token) y controla el acceso por **roles**. El frontend en React lo consume por HTTP.

**Equipo:** Matías Franke · Florencia Ferrada · Matías Letelier

## Tecnologías

Java 17 · Spring Boot 3.3 · Spring Web · Spring Data JPA · Spring Security · Bean Validation · MySQL (H2 para pruebas y demo) · JJWT 0.12 · springdoc-openapi (Swagger UI) · Maven.

## Estructura del proyecto

```
levelup-backend/
├── pom.xml                                     Dependencias y configuración de Maven
├── .gitignore                                  Archivos que no se suben (target/, .idea/, etc.)
├── README.md                                   Este archivo
└── src/
    ├── main/
    │   ├── java/cl/levelup/tienda/
    │   │   ├── LevelUpApplication.java         Punto de entrada de Spring Boot
    │   │   ├── model/                          Entidades JPA (tablas de la base de datos)
    │   │   │   ├── Rol.java                    Roles: administrador, vendedor, cliente
    │   │   │   ├── Usuario.java                Tabla usuarios (contraseña guardada con BCrypt)
    │   │   │   ├── Producto.java               Tabla productos (precio y stock)
    │   │   │   ├── Pedido.java                 Tabla pedidos (cliente, entrega, estado y totales)
    │   │   │   └── PedidoItem.java             Tabla pedido_items (líneas de cada pedido)
    │   │   ├── repository/                     Acceso a datos con Spring Data JPA
    │   │   │   ├── UsuarioRepository.java
    │   │   │   ├── ProductoRepository.java     Incluye consulta con bloqueo para descontar stock
    │   │   │   └── PedidoRepository.java
    │   │   ├── service/                        Lógica de negocio
    │   │   │   ├── AuthService.java            Login, registro (mayor de 18), renovación de tokens
    │   │   │   ├── ProductoService.java        CRUD de productos
    │   │   │   ├── PedidoService.java          Compra: valida stock, descuento Duoc, IVA, estado y descuento de stock
    │   │   │   └── UsuarioService.java         Listar usuarios, cambiar rol, eliminar
    │   │   ├── controller/                     Endpoints REST documentados en Swagger
    │   │   │   ├── AuthController.java         /api/auth
    │   │   │   ├── ProductoController.java     /api/productos
    │   │   │   ├── PedidoController.java       /api/pedidos
    │   │   │   └── UsuarioController.java      /api/usuarios
    │   │   ├── dto/                            Objetos que entran y salen por la API (JSON)
    │   │   ├── security/
    │   │   │   ├── JwtService.java             Genera y verifica los tokens JWT
    │   │   │   └── JwtAuthFilter.java          Lee el token de cada petición y autentica al usuario
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java         Reglas de acceso por ruta y rol, CORS, BCrypt
    │   │   │   ├── OpenApiConfig.java          Swagger con botón Authorize (Bearer JWT)
    │   │   │   └── DatosIniciales.java         Carga 10 productos y 3 usuarios si la base está vacía
    │   │   └── exception/                      Errores en JSON ({"mensaje": ..., "errores": {...}})
    │   └── resources/
    │       ├── application.properties          Configuración con MySQL
    │       └── application-h2.properties       Perfil h2: base en memoria, sin instalar nada
    └── test/
        ├── java/cl/levelup/tienda/ApiIntegracionTest.java   Pruebas de integración con MockMvc
        └── resources/application-test.properties
```

## Cómo correrlo

Requisitos: JDK 17 o superior y Maven.

**Con MySQL** (la base `levelup_gamer` se crea sola; solo necesitas MySQL encendido en el puerto 3306):

```bash
mvn spring-boot:run
```

Por defecto usa usuario `root` y clave `root`. Para cambiarlo:

```bash
# Mac / Linux
export DB_USER=mi_usuario
export DB_PASSWORD=mi_clave
# Windows (cmd)
set DB_USER=mi_usuario
set DB_PASSWORD=mi_clave
```

**Sin MySQL** (base H2 en memoria, los datos se pierden al apagar):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Pruebas de integración:

```bash
mvn test
```

## URLs

| Qué | URL |
|---|---|
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Documentación OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

En Swagger: hacer login en `POST /api/auth/login`, copiar el `accessToken`, presionar **Authorize** y pegarlo.

## Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| administrador | admin@levelup.cl | admin123 |
| vendedor | vendedor@levelup.cl | vendedor123 |
| cliente | cliente@levelup.cl | cliente123 |

Quien se registra en `POST /api/auth/register` queda con rol cliente.

## Endpoints

| Método | Ruta | Quién puede | Qué hace |
|---|---|---|---|
| POST | /api/auth/login | Público | Devuelve accessToken, refreshToken y usuario |
| POST | /api/auth/register | Público | Registra un cliente (mayor de 18 años) |
| POST | /api/auth/refresh | Público (con refresh token) | Entrega tokens nuevos |
| GET | /api/auth/me | Autenticado | Usuario dueño del token |
| GET | /api/productos | Público | Lista productos con stock |
| GET | /api/productos/{id} | Público | Detalle de un producto |
| POST | /api/productos | Administrador | Crea un producto |
| PUT | /api/productos/{id} | Administrador | Edita un producto y su stock |
| DELETE | /api/productos/{id} | Administrador | Elimina un producto |
| POST | /api/pedidos | Cliente, administrador | Compra: valida stock, calcula descuento Duoc e IVA |
| GET | /api/pedidos | Administrador, vendedor | Lista todos los pedidos |
| GET | /api/pedidos/{id} | Personal o dueño del pedido | Detalle de un pedido |
| DELETE | /api/pedidos/{id} | Administrador | Elimina un pedido |
| GET | /api/usuarios | Administrador | Lista usuarios |
| PUT | /api/usuarios/{id}/rol | Administrador | Cambia el rol de un usuario |
| DELETE | /api/usuarios/{id} | Administrador | Elimina un usuario (no a sí mismo) |

Códigos de error: `400` datos inválidos, `401` sin sesión o token inválido, `403` sin permisos, `404` no existe, `409` conflicto (correo repetido o stock insuficiente).

## Reglas de negocio

- **Stock:** una compra con más unidades que el stock se rechaza con 409. Si el pago es exitoso se descuenta; si es fallida no.
- **Estado del pedido:** `Compra exitosa` o `Compra fallida` (siempre se guarda el pedido).
- **Totales en el servidor:** subtotal con precios de la base de datos; descuento del 20% si el correo del usuario termina en `@duoc.cl` o `@duocuc.cl`; IVA del 19% sobre el valor con descuento.
- **Registro:** nombre de 2 a 100 caracteres, correo válido y único, mayor de 18 años y contraseña de al menos 6 caracteres.

## Seguridad (JWT)

- Contraseñas con **BCrypt**.
- **Access token** de 15 minutos y **refresh token** de 7 días (HS256, claims `sub`, `rol` y `tipo`). El refresh token se rota en cada renovación y un access token no sirve como refresh ni al revés.
- La API no guarda sesión en el servidor (stateless): cada petición lleva `Authorization: Bearer <token>`.
- El acceso por rol se define en `SecurityConfig` (rutas) y con `@PreAuthorize` en los controladores; el detalle de "dueño del pedido" se revisa en `PedidoService`.
- CORS permitido solo para `http://localhost:3000` y `http://127.0.0.1:3000`.

## Variables de entorno

| Variable | Por defecto | Para qué |
|---|---|---|
| DB_USER | root | Usuario de MySQL |
| DB_PASSWORD | root | Clave de MySQL |
| JWT_SECRET | clave de desarrollo | Secreto para firmar los JWT (mínimo 32 caracteres; cambiarlo fuera del desarrollo) |

## Declaración de uso de IA

Para el desarrollo de este proyecto (tienda online Level-Up Gamer, Evaluación Parcial 3 de DSY1104) se utilizó la herramienta de inteligencia artificial Claude (Anthropic) como apoyo en las siguientes tareas:

- Desarrollo del backend en Spring Boot (entidades, servicios, controladores, seguridad con JWT y pruebas de integración).
- Elaboración de material de apoyo para el estudio y preparación de la presentación oral del proyecto.
