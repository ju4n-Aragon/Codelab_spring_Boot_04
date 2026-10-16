# cliente-service

Microservicio de gestión de clientes — **Codelab 04: manejo global de excepciones**.
Spring Boot 3 · Java 17 · Maven · JPA · H2 embebido (modo PostgreSQL).

## Estructura

```
src/main/java/com/denkitronik/clienteservice/
├── ClienteServiceApplication.java
├── domain/
│   ├── entities/      Cliente, Region
│   ├── exception/     ClienteNotFoundException, ClienteServiceException
│   ├── repositories/  IClienteDao
│   └── services/      IClienteService, ClienteServiceImpl
└── delivery/
    ├── rest/          ClienteRestController
    └── exception/     GlobalExceptionHandler, ApiErrorResponse
```

## Ejecutar

```bash
mvn spring-boot:run     # levanta en http://localhost:8080
mvn test                # corre los tests
```

No requiere Docker ni base de datos externa: usa `jdbc:h2:mem:testdb;MODE=PostgreSQL`.

## Endpoints (prefijo `/api/v1/cliente-service`)

| Método | Ruta              | Éxito          | Error                                  |
|--------|-------------------|----------------|----------------------------------------|
| GET    | `/clientes/{id}`  | 200 + Cliente  | 404 `ApiErrorResponse`                 |
| DELETE | `/clientes/{id}`  | 204 sin body   | 404 `ApiErrorResponse`                 |
| POST   | `/clientes`       | 201 + Cliente  | 400 mapa `campo → mensaje`             |
| PUT    | `/clientes/{id}`  | 201 + Cliente  | 404 `ApiErrorResponse` / 400 validación |

Extra: `GET /clientes` lista todos los clientes.

### Formato de error

```json
{
  "status": 404,
  "error": "Not Found",
  "mensaje": "Cliente con id 999 no encontrado",
  "timestamp": "2025-04-20T14:30:00.123"
}
```

## Mapeo de excepciones

| Excepción                         | HTTP | Body                       | Log   |
|-----------------------------------|------|----------------------------|-------|
| `ClienteNotFoundException`        | 404  | `ApiErrorResponse`         | WARN  |
| `ClienteServiceException`         | 500  | `ApiErrorResponse` genérico | ERROR |
| `MethodArgumentNotValidException` | 400  | mapa campo → mensaje       | DEBUG |
| `Exception` (genérica)            | 500  | `ApiErrorResponse` genérico | ERROR |
