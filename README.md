# 🚀 SmartLogix - Arquitectura de Microservicios

## 📌 Descripción General

SmartLogix es una plataforma basada en arquitectura de microservicios que permite gestionar autenticación, pedidos, inventario y envíos de manera escalable, desacoplada y resiliente.

Este proyecto está construido con **Spring Boot**, utilizando herramientas del ecosistema Spring Cloud como **Eureka**, **API Gateway**, **Config Server** y **JWT** para la seguridad.

---

## 🏗️ Arquitectura del Proyecto

El sistema está compuesto por los siguientes microservicios:

```plaintext
Backend-SmartLogix
│
├── microservice-config       # Config Server (configuración centralizada)
├── microservice-eureka       # Service Discovery (Eureka Server)
├── microservice-gateway      # API Gateway (punto de entrada + JWT Filter)
├── microservice-auth         # Servicio de autenticación y autorización (JWT)
├── microservice-inventario   # Gestión de inventario
├── microservice-pedido       # Gestión de pedidos
├── microservice-envío        # Gestión de envíos
└── pom.xml                   # Proyecto padre (multi-module)
```

---

## 🔧 Tecnologías Utilizadas

- Java 17+
- Spring Boot 3
- Spring Cloud
- Eureka Server (Service Discovery)
- Spring Cloud Gateway
- Config Server
- Spring Security + JWT (JSON Web Tokens)
- PostgreSQL (una base de datos por microservicio)
- Maven (multi-módulo)
- Docker (opcional)

---

## 📡 Componentes Clave

### 🔹 Config Server (`microservice-config`)
Centraliza la configuración de todos los microservicios.

- Permite cambiar configuraciones sin recompilar
- Usa archivos `.yml` centralizados

---

### 🔹 Eureka Server (`microservice-eureka`)
Registro de servicios (Service Discovery).

- Cada microservicio se registra automáticamente
- Permite comunicación dinámica entre servicios
- Puerto: `:8761`

---

### 🔹 API Gateway (`microservice-gateway`)
Punto de entrada único al sistema.

- Maneja rutas como:
  - `/api/auth`
  - `/api/pedido`
  - `/api/inventario`
  - `/api/envio`
- Aplica el **JWT Filter** antes de enrutar cada petición
- Puerto: `:8080`

---

### 🔹 Auth Service (`microservice-auth`)
Servicio de autenticación y autorización basado en **JWT**.

**Responsabilidades:**
- Registro de nuevos usuarios
- Login y generación de tokens JWT
- Validación y refresco de tokens
- Gestión de roles y permisos

**Puerto:** `:8081`

---

### 🔹 Microservicios de Negocio

#### 📦 Pedido Service (`microservice-pedido`)
Gestiona los pedidos del sistema.

**Responsabilidades:**
- Crear pedidos
- Consultar pedidos
- Comunicarse con Inventario y Envío

**Puerto:** `:8083`

---

#### 📊 Inventario Service (`microservice-inventario`)
Gestiona el stock de productos.

**Responsabilidades:**
- Verificar disponibilidad de productos
- Actualizar stock tras un pedido

**Puerto:** `:8082`

---

#### 🚚 Envío Service (`microservice-envio`)
Gestiona los envíos generados por los pedidos.

**Responsabilidades:**
- Crear registros de envío
- Actualizar estado de entrega

**Puerto:** `:8084`

---

## 🔄 Flujo de Comunicación

1. El **Frontend (React :3000)** realiza una petición al **API Gateway (:8080)**
2. El Gateway aplica el **JWT Filter** — valida el token antes de enrutar
3. Si la ruta es `/api/auth`, redirige al **Auth Service** para login/registro
4. Para rutas protegidas, el Gateway redirige al microservicio correspondiente
5. El microservicio de **Pedido**:
   - Consulta **Inventario** para validar stock disponible
   - Llama a **Envio** para generar el envío
6. Se retorna la respuesta al cliente

---

## 🗄️ Base de Datos

Cada microservicio tiene su propia base de datos (principio de independencia):

| Microservicio | Base de Datos  | Puerto DB |
|---------------|----------------|-----------|
| Auth          | `auth_db`      | PostgreSQL |
| Pedido        | `pedidos_db`   | PostgreSQL |
| Inventario    | `inventario_db`| PostgreSQL |
| Envio         | `envios_db`    | PostgreSQL |

---

## 🗺️ Diagrama del Ecosistema

```
                        ┌─────────────────────────────────────────────┐
                        │           INFRAESTRUCTURA / SOPORTE          │
                        │                                               │
                        │  ┌─────────────────┐  ┌──────────────────┐  │
                        │  │  Eureka Server  │  │  Config Server   │  │
                        │  │  :8761          │  │  (centralizado)  │  │
                        │  └────────┬────────┘  └────────┬─────────┘  │
                        └──────────┼──────────────────────┼────────────┘
                                   │  (registro/config)   │
              ┌────────────────────┼──────────────────────┼────────────────────┐
              │                    ▼                        ▼                   │
              │         ┌──────────────────────────────────────────┐           │
              │         │              API GATEWAY :8080            │           │
              │         │         Spring Cloud Gateway              │           │
              │         │    ┌──────────────────────────────┐       │           │
              │         │    │        JWT Filter             │       │           │
              │         │    │   (valida antes de enrutar)  │       │           │
              │         │    └──────────────────────────────┘       │           │
              │         └──────┬──────┬───────────┬─────────┬───────┘           │
              │                │      │           │         │                    │
  ┌───────────┘                │      │           │         │                    └──────────┐
  │  Frontend                  ▼      ▼           ▼         ▼                               │
  │  React :3000    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐   │
  │        ─────►   │ Auth Service │ │  Inventario  │ │   Pedidos    │ │    Envios    │   │
  │                 │    :8081     │ │    :8082     │ │    :8083     │ │    :8084     │   │
  └─────────────    │ Controller   │ │ Controller   │ │ Controller   │ │ Controller   │   │
                    │ Service      │ │ Service      │ │ Service      │ │ Service      │   │
                    │ Repo (JPA)   │ │ Repo (JPA)   │ │ Repo (JPA)   │ │ Repo (JPA)   │   │
                    └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘   │
                           │                │                │                │
                           ▼                ▼                ▼                ▼
                    ┌────────────┐  ┌──────────────┐ ┌──────────────┐ ┌─────────────┐
                    │  auth_db   │  │inventario_db │ │  pedidos_db  │ │  envios_db  │
                    │ PostgreSQL │  │  PostgreSQL  │ │  PostgreSQL  │ │  PostgreSQL │
                    └────────────┘  └──────────────┘ └──────────────┘ └─────────────┘

 Comunicación interna entre microservicios (Pedido ↔ Inventario ↔ Envio):

  ┌──────────────┐        verifica stock        ┌──────────────┐
  │   Pedidos    │ ─────────────────────────►   │  Inventario  │
  │    :8083     │ ◄─────────────────────────   │    :8082     │
  └──────┬───────┘        stock actualizado      └──────────────┘
         │
         │  genera envío
         ▼
  ┌──────────────┐
  │    Envios    │
  │    :8084     │
  └──────────────┘
```

---

## ⚙️ Configuración y Ejecución

### 1️⃣ Clonar el proyecto
```bash
git clone <repo-url>
cd Backend-SmartLogix
```

### 2️⃣ Orden de arranque recomendado

> ⚠️ Es importante respetar este orden para que el registro y la configuración funcionen correctamente.

```
1. microservice-config    → Primero (provee configuración a todos)
2. microservice-eureka    → Segundo (registro de servicios)
3. microservice-gateway   → Tercero (punto de entrada)
4. microservice-auth      → Cuarto (autenticación)
5. microservice-inventario
6. microservice-pedido
7. microservice-envio
```

### 3️⃣ Variables de entorno por microservicio

Cada servicio requiere configurar su conexión a PostgreSQL. Ejemplo para `auth`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: tu_password
```

### 4️⃣ Puertos expuestos

| Microservicio       | Puerto |
|---------------------|--------|
| Frontend (React)    | :3000  |
| API Gateway         | :8080  |
| Auth Service        | :8081  |
| Inventario Service  | :8082  |
| Pedidos Service     | :8083  |
| Envios Service      | :8084  |
| Eureka Server       | :8761  |

---

## 🔐 Seguridad

- Todas las rutas (excepto `/api/auth/login` y `/api/auth/register`) requieren un **JWT válido**
- El token se genera al hacer login en el **Auth Service**
- El **JWT Filter** en el Gateway valida el token antes de enrutar la petición
- Los tokens incluyen información del usuario y sus roles

**Flujo de autenticación:**
```
Cliente → POST /api/auth/login → Auth Service → JWT Token
Cliente → GET /api/pedido (+ Bearer Token) → Gateway → JWT Filter ✅ → Pedidos Service
```

---

## 📁 Estructura de Paquetes por Microservicio

```
microservice-{nombre}
└── src/main/java/com/smartlogix/{nombre}
    ├── controller/     # Endpoints REST
    ├── service/        # Lógica de negocio
    ├── repository/     # Acceso a datos (JPA)
    ├── model/          # Entidades
    ├── dto/            # Objetos de transferencia
    └── config/         # Configuraciones del servicio
```

---

## 🐳 Docker (Opcional)

```bash
# Construir y levantar todos los servicios
docker-compose up --build

# Detener todos los servicios
docker-compose down
```

---

## 👥 Contribución

1. Haz un fork del repositorio
2. Crea una rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit: `git commit -m "feat: agrega nueva funcionalidad"`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

---

*SmartLogix © 2024 — Arquitectura de Microservicios con Spring Boot 3*
