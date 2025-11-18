# CamisApi — Tienda de Camisetas de Futbol

CamisApi es una aplicación diseñada como una tienda online para la venta de camisetas de futbol. Muestra a los usuarios interesados en el mundo del fútbol un catálogo de camisetas únicas, donde pueden seleccionar las que más les guste y comprarlas. Primero la añadirían al carrito de la compra, para más tarde realizar la compra. Lo novedoso de esta tienda es que todas las camisetas que se venden son únicas y exclusivas, permitiendo administrar la reserva y venta de cada camiseta, no hay otra en todo el mundo. Así que solo puede existir una unidad disponible, evitando que dos personas quieran comprar el mismo producto. 

Esta aplicación también cuenta con un proceso de pedidos, donde el usuario puede elegir una de las camisetas, elegir si comprarla o no y recibir el producto. Así nuestros clientes quedarán más satisfechos. 

## Características principales

- Productos únicos sin posibilidad de duplicados.
- Sistema de reserva automática al incorporar productos al carrito.
- Gestión completa de pedidos con snapshot de productos.
- Autenticación y autorización mediante Spring Security + JWT.
- Arquitectura híbrida entre NoSQL y SQL.
- Exposición de servicios mediante REST, WebSocket y GraphQL.
- Sistema de pruebas automatizadas con JUnit, Mockito y TestContainers.

---

## Arquitectura de datos

| Tipo de información | Base de Datos | Motivo |
|---------------------|----------------|------------------------------------------------|
| Catálogo de productos únicos | MongoDB | Flexibilidad y consultas rápidas |
| Usuarios, carritos y pedidos | PostgreSQL | Consistencia transaccional y ACID |

---

## Ciclo de estados del producto
- **DISPONIBLE**: el producto está libre.
- **RESERVADO**: asociado temporalmente a un carrito.
- **VENDIDO**: el producto ha sido comprado y no vuelve al catálogo.

### Reglas de transición:
- No se permite volver a estado inicial desde VENDIDO.
- RESERVADO expira si el carrito es eliminado o caduca.
- Un producto no puede ser reservado dos veces simultáneamente.

---

## Tecnologías utilizadas

### Backend
- Java 25
- Spring Boot 3.5.7
- Spring MVC / Spring Data JPA / Spring Data MongoDB
- Spring Security con JWT
- WebSockets
- GraphQL (implementación en módulo carrito)
- Gradle

### Bases de datos
- MongoDB (productos)
- PostgreSQL (usuarios, carritos, pedidos)
- Redis (cache)
- H2 (entorno de pruebas)

### Testing
- JUnit 5
- Mockito
- TestContainers

### Documentación API
- OpenAPI

---

## Seguridad

El proyecto integra Spring Security como capa de protección en la API, gestionando:

- Autenticación de usuarios mediante credenciales verificadas.
- Autorización por roles mediante anotaciones (`USER` y `ADMIN`).
- Validación de sesión mediante tokens JWT.
- Arquitectura sin estado mediante SessionCreationPolicy.STATELESS.
- Filtro personalizado para interceptar y validar tokens en cada petición.

---

## Endpoints principales

Los módulos principales publicados en la API REST son:

- `/productos` 
- `/carritos`
- `/pedidos`
- `/users`
- `/auth` = Autenticacion
---

## Configuración del proyecto

### Perfiles de entorno (Spring Profiles)

| Entorno | Archivo | Descripción |
|---------|---------------------------|-----------------------------|
| General | application.properties | Configuración base |
| Desarrollo | application-dev.properties | Conexiones locales |
| Docker | application-docker.properties | Despliegue en contenedores |
| Testing | application-test.properties | Uso de H2 y servicios deshabilitados |

### Requisitos

- JDK 21 o superior (recomendado)
- Docker (opcional)
- MongoDB 5+
- PostgreSQL 14+
- Redis 6+

### Ejecución

#### Opción 1 — Desde IDE
1. Importar proyecto en IntelliJ IDEA.
2. Seleccionar el perfil `dev`.
3. Ejecutar la aplicación.

#### Opción 2 — Docker Compose

## Equipo:
- Marcos Jodar
- Jorge Morgado
- Cristian Ortega
- Angel Sanchez
