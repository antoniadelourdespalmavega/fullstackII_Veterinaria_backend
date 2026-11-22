# Veterinaria San Marcos – Backend (Spring Boot)

API REST que guarda en una base de datos los usuarios, las citas, las fichas clínicas y las compras de la tienda de la
Veterinaria San Marcos. Es el backend del proyecto React `Veterinaria_react`.

- **Spring Boot 3.5** (Java 17 o superior) con Spring Web, Spring Data JPA y Spring Security
- **Base de datos H2 en archivo** (`./data/veterinaria`): los datos no se pierden al apagar el servidor
- **Autenticación con JWT** y roles: administrador, secretaria, veterinario y cliente
- **Swagger** para ver y probar los endpoints

## Cómo ejecutarlo

```bash
.\mvnw spring-boot:run
```

(En PowerShell hay que escribir `.\` antes de `mvnw`. La primera vez descarga Maven y las librerías, así que demora unos minutos. También sirve ejecutar `VeterinariaBackendApplication` desde IntelliJ. Se necesita Java 17+.)

| Qué | Dirección |
|---|---|
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Consola de la base de datos | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/veterinaria`, usuario `sa`, sin contraseña) |

La primera vez que arranca, la clase `DataLoader` carga los datos iniciales (8 usuarios, 3 citas y 6 fichas). Las tablas son `USUARIOS`, `CITAS`, `FICHAS`, `PEDIDOS` y `PEDIDO_ITEMS`.
Los usuarios de prueba están en el `README` del frontend. Para volver a empezar de cero, apagar el servidor y borrar la carpeta `data`.

## Cómo está organizado

```
src/main/java/com/veterinaria/backend/
├── model/        Entidades = tablas: Usuario, Cita, Ficha (y DetallePago, que va dentro de Ficha), Pedido y PedidoItem
├── repository/   Acceso a la base de datos (Spring Data JPA)
├── service/      Lógica de negocio y reglas (choque de horarios, domingos, correo único...)
├── controller/   Endpoints REST (documentados en Swagger)
├── security/     JwtService (crea y verifica tokens) y JwtAuthFilter (revisa el token en cada petición)
├── config/       SecurityConfig (rutas públicas, CORS, BCrypt), OpenApiConfig (Swagger), DataLoader (datos iniciales)
└── exception/    Errores con mensaje claro: { "mensaje": "..." }
```

## Autenticación (JWT)

1. `POST /api/auth/login` con correo y clave → devuelve `token` (dura 30 minutos), `tokenRenovacion` (7 días) y el usuario.
2. El frontend manda el `token` en cada petición: `Authorization: Bearer <token>`.
3. Si el token venció, `POST /api/auth/refresh` con el `tokenRenovacion` entrega uno nuevo.
4. Las contraseñas se guardan cifradas con BCrypt y nunca salen en las respuestas.
5. Una cuenta desactivada no puede iniciar sesión ni usar un token que ya tenía.

Para probar en Swagger: hacer login, copiar el `token` y pegarlo en el botón **Authorize**.

## Endpoints

| Método | Ruta | Quién puede usarlo |
|---|---|---|
| POST | `/api/auth/login` | público |
| POST | `/api/auth/refresh` | público (con token de renovación) |
| POST | `/api/auth/registro` | público (crea un cliente) |
| GET | `/api/usuarios`, `/api/usuarios/{id}` | administrador |
| POST | `/api/usuarios` | administrador |
| PUT | `/api/usuarios/{id}` | administrador |
| PATCH | `/api/usuarios/{id}/activo` | administrador (activar o desactivar) |
| DELETE | `/api/usuarios/{id}` | administrador |
| GET | `/api/citas`, `/api/citas/{id}` | personal: todas; cliente: solo las suyas |
| GET | `/api/citas/horarios-ocupados?fecha=AAAA-MM-DD` | público (solo horas, sin datos personales) |
| POST | `/api/citas/solicitud` | público (queda Pendiente) |
| POST | `/api/citas` | secretaria, administrador (queda Confirmada) |
| PUT | `/api/citas/{id}/gestion` | secretaria, administrador (confirmar o reagendar) |
| PATCH | `/api/citas/{id}/anular` | secretaria, administrador |
| DELETE | `/api/citas/{id}` | administrador |
| GET | `/api/fichas`, `/api/fichas/{id}` | personal: todas; cliente: solo las suyas |
| POST | `/api/fichas` | veterinario, secretaria, administrador |
| PUT | `/api/fichas/{id}` | veterinario, secretaria, administrador (también registra el pago) |
| DELETE | `/api/fichas/{id}` | administrador |
| POST | `/api/pedidos` | público (la tienda se usa sin iniciar sesión; se llama cuando el pago fue aprobado) |
| GET | `/api/pedidos`, `/api/pedidos/{id}` | administrador, secretaria |
| DELETE | `/api/pedidos/{id}` | administrador |

Códigos de error: `400` datos inválidos, `401` sin sesión o token inválido, `403` sin permiso para esa acción,
`404` no existe, `409` conflicto (correo repetido, horario ocupado).

## Reglas de negocio

- Un veterinario no puede tener dos citas **confirmadas** el mismo día y bloque.
- No se agenda en el pasado ni en domingo.
- Una solicitud del cliente nace `Pendiente` y sin veterinario; una cita de Recepción nace `Confirmada`.
- Anular una cita no la borra: queda `Cancelada` y libera el horario.
- El correo de un usuario es único. Desactivar una cuenta no la borra.
- Una compra (`Pedido`) guarda al comprador, el método de pago, el código de transacción y sus productos. El **total lo calcula el backend** sumando los productos, y **nunca se guardan datos de tarjeta**.
- Un administrador no puede desactivarse ni eliminarse a sí mismo.
