# Documentación del Backend - Poker Online ♦️

> Backend Spring Boot para una aplicación de póker multijugador en tiempo real, con autenticación JWT, lógica de partidas/torneos, moderación y sistema de logros.

**Autor:** Marc Martín  
**Licencia:** MIT  
**Última actualización:** 30 de septiembre de 2025

---

## 📑 Índice

1. [Introducción](#-introducción)
2. [Tecnologías utilizadas](#-tecnologías-utilizadas)
3. [Arquitectura y estructura](#-arquitectura-y-estructura)
4. [Requisitos y configuración](#-requisitos-y-configuración)
5. [Inicio de la aplicación](#-inicio-de-la-aplicación)
6. [Autenticación y seguridad](#-autenticación-y-seguridad)
7. [Modelo de errores](#-modelo-de-errores)
8. [Dominios y endpoints](#-dominios-y-endpoints)
9. [Reglas de negocio clave](#-reglas-de-negocio-clave)
10. [WebSocket: comunicación en tiempo real](#-websocket-comunicación-en-tiempo-real)
11. [Sistema de bots con IA](#-sistema-de-bots-con-ia)
12. [Sistema de torneos](#-sistema-de-torneos)
13. [Sistema de logros](#sistema-de-logros)
14. [Moderación y sanciones](#moderacion-y-sanciones)
15. [Estadísticas y ranking](#-estadísticas-y-ranking)
16. [Modo espectador](#modo-espectador)
17. [Datos y persistencia](#-datos-y-persistencia)
18. [Jobs programados](#-jobs-programados)
19. [Pruebas con Postman](#-pruebas-con-postman)
20. [Consideraciones de despliegue](#-consideraciones-de-despliegue)
21. [Roadmap](#roadmap)

---

## 🏠 Introducción

Este proyecto representa el backend de una aplicación web y móvil de póker en línea, diseñado como proyecto de autoaprendizaje y desarrollo profesional. El objetivo es ofrecer una experiencia de póker realista y multijugador, con:

- ✅ Autenticación segura mediante JWT
- ✅ Gestión completa de partidas en tiempo real
- ✅ Lógica de juego Texas Hold'em con evaluación real de manos
- ✅ Sistema de torneos individuales y por equipos
- ✅ Inteligencia artificial para bots con comportamiento realista
- ✅ Sistema de logros y estadísticas
- ✅ Moderación con sanciones automáticas
- ✅ Mesas privadas con fichas temporales
- ✅ Modo espectador

---

## 🎯 Tecnologías utilizadas

- **Java 17** + **Spring Boot 3**
- **MySQL 8** (JPA/Hibernate)
- **JWT** (Spring Security) para autenticación
- **WebSocket** para comunicación en tiempo real
- **Gradle** para gestión de dependencias
- **Lombok** para simplificar código
- **Postman** para testing de API
- **Docker** para contenedores (MySQL)

---

## 📂 Arquitectura y estructura

```
poker-backend/
├── admin/                  # Código de uso de administrador
│   ├── controller/
│   ├── dto/
│   ├── service/
├── bot/                    # Lógica de los bots de juego
├── chat/                   # Lógica e implementación del chat
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── repository/
│   └── model/
├── config/                 # Seguridad (JWT), CORS, WebSocket
├── controller/             # Controladores REST
├── dto/                    # DTOs de entrada/salida
├── estadisticas/           # Sistema de estadísticas
├── exception/              # Excepciones + GlobalExceptionHandler
├── logros/                 # Sistema de logros
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── LogroDataLoader.java
├── moderacion/             # Sistema de sanciones
│   ├── controller/
│   ├── dto/
│   ├── job/
│   ├── service/
│   ├── repository/
│   └── model/
├── torneo/                 # Torneos + equipos + sala de espera
│   ├── controller/
│   ├── dto/
│   ├── equipos/
│   ├── equipos/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   ├── scheduler/
│   ├── service/
│   ├── repository/
│   ├── websocket/
│   └── model/
├── service/                # Lógica de negocio
│   ├── MesaService
│   ├── TurnoService
│   ├── BarajaService
│   ├── EvaluadorManoService
│   ├── BotService
│   └── ...
├── util/                   # Filtrado de palabras 
├── websocket/              # WebSocketService (eventos en tiempo real)
├── repository/             # Repositorios JPA
├── model/                  # Entidades JPA
├── DataLoader.java
└── PokerBackendApplication.java

poker-frontend/                          # (Futuro) Frontend React/Flutter

docs/                              # Documentación Markdown (GitHub Pages)
├── index.md                       # Índice y selector de documentación
├── backend.md                     # Esta documentación
├── amigos.md                      # Documentación exclusiva para el sistema de amigos (aún no implementado)
└── frontend.md                    # Documentación para el frontend (aún no implementado)
```

---

## 📋 Requisitos y configuración

### Requisitos previos

- **Java 17+**
- **MySQL 8** (local o Docker)
- **Gradle** (o usar wrapper `./gradlew`)
- **IDE recomendado:** IntelliJ IDEA, VSCode o Eclipse

### Variables de entorno

Configura estas variables en `application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/pokerdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=user
spring.datasource.password=pass
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=super-secreto-cambiar-en-produccion
app.jwt.expiration=86400000

# Jobs programados
jobs.sanciones.delay=60000
```

### Docker MySQL (inicio rápido)

```bash
docker run --name mysql_poker \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=pokerdb \
  -e MYSQL_USER=user \
  -e MYSQL_PASSWORD=pass \
  -p 3306:3306 -d mysql:8.0
```

Verificar que el contenedor está ejecutándose:

```bash
docker ps
```

---

## 🚀 Inicio de la aplicación

### 1. Clonar el repositorio

```bash
git clone https://github.com/Marukunai/poker_online.git
cd poker_online/poker-backend
```

### 2. Ejecutar el backend

**Opción A: Línea de comandos**

```bash
./gradlew bootRun
```

**Opción B: Desde IDE**

1. Importa el proyecto como proyecto Gradle
2. Asegúrate de que se descarguen las dependencias
3. Ejecuta `PokerBackendApplication.java`

### 3. Verificar funcionamiento

```bash
# Health check
GET http://localhost:8080/actuator/health

# Listar mesas
GET http://localhost:8080/api/mesas
```

---

## 🔐 Autenticación y seguridad

### JWT (JSON Web Token)

Los usuarios se autentican mediante email y contraseña, obteniendo un token JWT que debe incluirse en todas las peticiones protegidas:

```http
Authorization: Bearer {token}
```

### Roles

- **USER**: Usuario estándar
- **ADMIN**: Administrador con permisos especiales

### Protección de endpoints

Los endpoints administrativos están protegidos con:

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

## ❌ Modelo de errores

`GlobalExceptionHandler` unifica las respuestas de error en formato JSON:

```json
{
  "error": "Mensaje legible del error",
  "status": 400,
  "timestamp": "2025-09-30T10:40:47.4738402"
}
```

### Excepciones específicas

| Excepción | Código HTTP | Descripción |
|-----------|-------------|-------------|
| `ResourceNotFoundException` | 404 | Recurso no encontrado |
| `UnauthorizedException` | 401 | No autorizado |
| `AlreadyInactiveException` | 400 | Recurso ya inactivo |
| `AlreadyHasAchievementException` | 400 | Logro ya obtenido |
| `ActiveSanctionExistsException` | 400 | Sanción activa existente |
| Otras excepciones | 500 | Error interno del servidor |

---

## 🌐 Dominios y endpoints

> **Base URL:** `http://localhost:8080`

### 🔑 Auth

#### Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@email.com",
  "password": "pass1234"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "alice@email.com",
  "password": "pass1234"
}
```

#### Perfil del usuario autenticado
```http
GET /api/auth/me
Authorization: Bearer {token}
```

---

### 👤 Usuario

#### Perfil extendido
```http
GET /api/user/profile
Authorization: Bearer {token}
```

Incluye sanciones actuales y estadísticas.

#### Actualizar perfil
```http
PUT /api/user/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "nuevoNombre",
  "avatarUrl": "https://..."
}
```

#### Cambiar contraseña
```http
POST /api/user/me/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "pass1234",
  "newPassword": "nuevaPass5678"
}
```

#### Ranking global
```http
GET /api/user/ranking
```

#### Historial de manos
```http
GET /api/user/historial
Authorization: Bearer {token}
```

#### Perfil público
```http
GET /api/user/public-profile/{userId}
```

#### Resumen completo
```http
GET /api/user/{userId}/resumen-completo
```

Incluye perfil + logros + historial de torneos + últimas 5 manos.

---

### 🃏 Mesas y turnos

#### Iniciar turnos en mesa
```http
POST /api/turnos/iniciar/{mesaId}
Authorization: Bearer {token}
```

Inicializa la baraja, reparte cartas y prepara la primera ronda.

#### Ver turno actual
```http
GET /api/turnos/actual/{mesaId}
```

#### Avanzar turno
```http
POST /api/turnos/avanzar/{mesaId}
Authorization: Bearer {token}
```

#### Realizar acción
```http
POST /api/turnos/accion/{mesaId}?accion=RAISE&cantidad=50
Authorization: Bearer {token}
```

**Acciones disponibles:** `FOLD`, `CHECK`, `CALL`, `RAISE`, `ALL_IN`

#### Avanzar fase
```http
POST /api/turnos/fase/{mesaId}/siguiente
Authorization: Bearer {token}
```

Cambia a la siguiente fase: Pre-Flop → Flop → Turn → River → Showdown

---

### 🎰 Mesas privadas

#### Crear mesa privada
```http
POST /api/mesas/privadas/crear
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Mesa de amigos",
  "maxJugadores": 6,
  "codigo": "AMIGOS123",
  "fichasTemporales": 10000,
  "smallBlind": 50,
  "bigBlind": 100
}
```

**Características:**
- Código de acceso personalizado
- Fichas temporales (no afectan saldo global)
- Máximo 10 millones de fichas por jugador
- Soporte para hasta 8 jugadores

#### Unirse a mesa privada
```http
POST /api/mesas/privadas/unirse
Content-Type: application/json

{
  "email": "usuario@email.com",
  "codigoAcceso": "AMIGOS123",
  "fichasSolicitadas": 5000
}
```

#### Añadir bot
```http
POST /api/mesas/privadas/{codigo}/add-bot
Authorization: Bearer {token}
```

**Restricciones:**
- Solo el creador puede añadir bots
- Limitado por `maxJugadores`
- Si la mesa se llena con humanos, se eliminan bots automáticamente

---

### 🏆 Torneos

#### Listar todos los torneos
```http
GET /api/torneos
```

#### Buscar torneo por nombre
```http
GET /api/torneos/torneo?nombre=...
```

#### Filtrar por estado
```http
GET /api/torneos/pendientes
GET /api/torneos/encurso
GET /api/torneos/finalizados
```

#### Ver torneo específico
```http
GET /api/torneos/{id}
```

#### Ver estado del torneo
```http
GET /api/torneos/{id}/estado
```

#### Ver nivel de ciegas
```http
GET /api/torneos/{id}/nivel-ciegas
```

#### Crear torneo
```http
POST /api/torneos
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Torneo Mensual",
  "buyIn": 1000,
  "premio": 10000,
  "maxJugadores": 50,
  "tipoTorneo": "ELIMINACION_DIRECTA"
}
```

#### Cambiar estado
```http
PATCH /api/torneos/{id}/estado?nuevoEstado=EN_CURSO
Authorization: Bearer {token}
```

#### Eliminar torneo
```http
DELETE /api/torneos/{id}
Authorization: Bearer {token}
```

---

### 👥 Equipos y miembros

#### Crear equipo
```http
POST /api/torneos/equipos
Authorization: Bearer {token}
Content-Type: application/json

{
  "torneoId": 1,
  "nombreEquipo": "Big Dogs",
  "capitanId": 1
}
```

#### Listar equipos de un torneo
```http
GET /api/torneos/equipos/torneo/{torneoId}
```

#### Ver equipo específico
```http
GET /api/torneos/equipos/{equipoId}
```

#### Actualizar puntos
```http
PUT /api/torneos/equipos/{equipoId}/puntos/{puntos}
Authorization: Bearer {token}
```

#### Eliminar equipo
```http
DELETE /api/torneos/equipos/{equipoId}
Authorization: Bearer {token}
```

#### Actualizar capitán
```http
PUT /api/torneos/equipos/actualizar-capitan
Authorization: Bearer {token}
Content-Type: application/json

{
  "equipoId": 1,
  "nuevoCapitanId": 4
}
```

**Permisos:** Solo capitán o admin pueden modificar.

#### Rankings

```http
GET /api/torneos/equipos/torneo/{torneoId}/ranking
GET /api/torneos/equipos/ranking/global
GET /api/torneos/equipos/ranking/anual/{year}
GET /api/torneos/equipos/ranking/mensual/{year}/{mes}
```

#### Estadísticas e historial
```http
GET /api/torneos/equipos/{equipoId}/estadisticas
GET /api/torneos/equipos/{equipoId}/historial
```

#### Gestión de miembros

```http
# Añadir miembro
POST /api/torneos/equipos/miembros
Authorization: Bearer {token}
Content-Type: application/json

{
  "equipoId": 1,
  "userId": 4
}

# Listar miembros
GET /api/torneos/equipos/miembros/equipo/{equipoId}

# Ver miembro específico
GET /api/torneos/equipos/miembros/equipo/{equipoId}/user/{userId}

# Eliminar miembro
DELETE /api/torneos/equipos/miembros/{equipoId}/{miembroId}
Authorization: Bearer {token}

# Eliminar todos los miembros
DELETE /api/torneos/equipos/miembros/equipo/{equipoId}
Authorization: Bearer {token}
```

---

### ⏳ Sala de espera de torneos

```http
# Registrarse
POST /api/torneos/espera/registrar?torneoId=1&userId=4
Authorization: Bearer {token}

# Ver participantes
GET /api/torneos/espera/{torneoId}

# Limpiar sala
DELETE /api/torneos/espera/{torneoId}
Authorization: Bearer {token}
```

---

### 🏅 Logros

#### Ver catálogo de logros
```http
GET /api/logros
```

#### Otorgar logro (admin)
```http
POST /api/logros/otorgar?userId=1&nombreLogro=Bluff Master
Authorization: Bearer {token}
```

**Respuesta:**
```json
{
  "message": "Logro otorgado correctamente"
}
```

#### Ver logros de usuario
```http
GET /api/logros/usuario/{userId}
```

Incluye flags `obtenido` y `fechaObtencion`.

#### Asignar logro
```http
POST /api/logros/usuario/{userId}/asignar/{logroId}
Authorization: Bearer {token}
```

#### Eliminar logro
```http
DELETE /api/logros/usuario/{userId}/eliminar/{logroId}
Authorization: Bearer {token}
```

**Nota:** Si el usuario ya tiene el logro, se lanza `AlreadyHasAchievementException` → 400.

---

### 🛡️ Moderación y sanciones

#### Aplicar sanción (admin)
```http
POST /api/admin/sanciones/aplicar
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded

userId=5&motivo=COMPORTAMIENTO_TOXICO&tipo=SUSPENSION_TEMPORAL&descripcion=Lenguaje ofensivo&diasDuracion=7
```

**Tipos de sanción:**
- `BLOQUEO_CUENTA`
- `SUSPENSION_TEMPORAL`
- `SUSPENSION_PERMANENTE`
- `PROHIBICION_CHAT`

**Prevención de duplicados:** Si ya existe una sanción activa equivalente, lanza `ActiveSanctionExistsException` → 400.

#### Crear sanción (usuario)
```http
POST /api/sanciones
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 5,
  "tipo": "SUSPENSION_TEMPORAL",
  "motivo": "COMPORTAMIENTO_TOXICO_EN_TORNEOS",
  "descripcion": "Lenguaje tóxico reiterado durante el torneo",
  "fechaFin": "2025-10-07T11:40:00Z"
}
```

#### Ver sanciones de usuario
```http
GET /api/sanciones/usuario/{userId}
```

#### Desactivar sanción
```http
DELETE /api/sanciones/{sancionId}
Authorization: Bearer {token}
```

**Nota:** Si ya está inactiva, lanza `AlreadyInactiveException` → 400.

---

### 📊 Estadísticas

```http
# Estadísticas de usuario
GET /api/estadisticas/usuario/{id}

# Ranking global
GET /api/estadisticas/ranking/global

# Ranking mensual
GET /api/estadisticas/ranking/mensual

# Historial de torneos
GET /api/torneos/usuario/{userId}/historial
```

---

### 👁️ Modo espectador

```http
# Unirse como espectador
POST /api/mesa/espectadores/{mesaId}/unirse
Authorization: Bearer {token}

# Salir de modo espectador
DELETE /api/mesa/espectadores/{mesaId}/salir
Authorization: Bearer {token}

# Listar espectadores
GET /api/mesa/espectadores/{mesaId}

# Ver datos de la mesa (espectador)
GET /api/mesa/espectadores/{mesaId}/datos
```

**Restricciones:**
- No ven cartas privadas de jugadores
- Observan pot, apuestas, acciones y chat público

---

## 🎬 Reglas de negocio clave

### Flujo de una partida

1. 🔐 Registro/Login
2. 🔍 Unión a una mesa
3. ▶️ Inicio de la partida
4. ♦️ Reparto de cartas privadas
5. ⏳ Turnos de acción (check, fold, call, raise, all-in)
6. 📊 Avance de fases (Pre-Flop → Flop → Turn → River → Showdown)
7. ⚖️ Evaluación de manos
8. 🌟 Reparto del pot (incluyendo side pots)
9. ⟳ Inicio de nueva ronda

### Roles en mesa

- **Dealer** (botón)
- **Small Blind**
- **Big Blind**
- **Jugadores normales**

Los roles rotan automáticamente en cada nueva mano.

### Control de fichas

- `User.fichas`: Fichas globales del usuario
- `UserMesa.fichasEnMesa`: Fichas activas en la mesa actual
- `UserMesa.totalApostado`: Lo apostado en la partida actual
- `Mesa.pot`: Bote total en juego

### Side pots

Cuando un jugador hace all-in con menos fichas que otros, se crean **side pots** para repartir proporcionalmente entre los jugadores elegibles.

### Timeout de turnos

- **60 segundos** por turno
- Si expira → FOLD forzado + sanción `INACTIVIDAD_EN_PARTIDAS` (advertencia)
- Notificación vía WebSocket

### Sanciones graves

Las sanciones activas tipo `BLOQUEO_CUENTA`, `SUSPENSION_TEMPORAL` o `SUSPENSION_PERMANENTE` impiden:
- Unirse a mesas
- Participar en torneos
- Iniciar partidas

Tres sanciones de estilo `ADVERTENCIA` conforman una sanción grave que deriva en `SUSPENSION_TEMPORAL`o `PROHIBICION_CHAT` dependiendo de los motivos de la sanción.

La sanción de `PROHIBICION_CHAT`, como su nombre indica, impide el uso del chat hasta la finalización de la sanción (automáticamente a 1 día)

### Prevención de duplicados

- **Logros:** No se pueden asignar dos veces (excepción)
- **Sanciones activas equivalentes:** No se apilan (excepción)

---

## 🔌 WebSocket: comunicación en tiempo real

### Eventos principales

| Evento | Descripción |
|--------|-------------|
| `turno` | Notifica de quién es el turno actual |
| `fase` | Cambio de fase + cartas comunitarias reveladas |
| `accion` | Acción realizada por un jugador (fold, call, raise...) |
| `bot_actuando` | Indica que un bot está procesando su turno |
| `showdown` | Revela ganadores, manos ganadoras y cartas |
| `sancion` | Notificación personalizada de sanción al jugador |
| `chat_bot` | Mensaje simulado enviado por un bot |

### Conexión

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/mesa/1', (message) => {
    console.log(JSON.parse(message.body));
  });
});
```

---

## 🤖 Sistema de bots con IA

Los bots son jugadores controlados por IA con comportamiento realista y estratégico.

### Características

- Representados como `User` con `esIA = true`
- Nombres únicos como `CPU-42`
- Solo disponibles en **mesas privadas**
- Retardo de 10-15 segundos simulando "pensamiento"
- No afectan fichas globales del sistema
- Con niveles de dificultad y estilos de juego diferentes para adaptarte y entrenar con cada uno de ellos

### Configuración

```java
User bot = User.builder()
  .email("cpu...@bot.com")
  .username("CPU-XX")
  .esIA(true)
  .nivelBot(DificultadBot.NORMAL)
  .estiloBot(EstiloBot.AGRESIVO)
  .build();
```

### Niveles de dificultad

| Nivel | Bluff | Slowplay | Evaluación contextual |
|-------|-------|----------|----------------------|
| **FACIL** | ❌ | ❌ | Decisiones simples |
| **NORMAL** | ⚠️ | ❌ | Usa draws y cartas conectadas |
| **DIFICIL** | ✅ | ✅ | Analiza fuerza + faroles estratégicos |

### Estilos de juego

| Estilo | Agresividad | Comportamiento |
|--------|-------------|----------------|
| **AGRESIVO** | Alto (1.4x) | Muchos raise y all-in |
| **CONSERVADOR** | Bajo (0.7x) | Cauto, se retira con facilidad |
| **LOOSE** | Medio (1.2x) | Juega muchas manos |
| **TIGHT** | Medio (0.8x) | Solo juega manos fuertes |
| **DEFAULT** | 1.0x | Equilibrado |

### Lógica de decisión

Los bots evalúan:
- Fuerza de su mano actual
- Conectividad de cartas (suited, conectadas)
- Posibilidad de flush/straight draw
- Contexto de la mesa (apuestas, fase, jugadores activos)
- Probabilidad de bluff o slowplay según dificultad

### Chat simulado

Los bots envían frases contextuales vía WebSocket:

```json
{
  "tipo": "chat_bot",
  "jugador": "CPU-42",
  "mensaje": "¡A ver si aguantas esta!"
}
```

Gestionado por el enum `FrasesBotChat.java`.

### Restricciones

- Solo el creador de la mesa puede añadir bots
- Limitados por `maxJugadores`
- Si un humano se une y la mesa está llena, se elimina automáticamente un bot
- Los bots se eliminan al finalizar la partida o abandonar la mesa

---

## 🏆 Sistema de torneos

### Características

- Torneos con buy-in, premios y rondas automáticas
- Soporte para torneos individuales y por equipos
- Sistema de eliminación directa o ranking por puntos
- Ciegas crecientes por nivel (blind levels)
- Integración completa con WebSocket

### Entidades principales

- `Torneo`: Configuración general
- `ParticipanteTorneo`: Inscripciones
- `TorneoMesa`: Mesas del torneo
- `BlindLevel`: Niveles de ciegas
- `EquipoTorneo`: Equipos participantes
- `MiembroEquipoTorneo`: Miembros de equipos

### Avance automático

El sistema `TorneoScheduler` (@Scheduled):
1. Inicia torneos cuando llega la fecha
2. Agrupa jugadores en mesas por rondas
3. Avanza de fase cuando queda 1 jugador por mesa
4. Asigna premios al finalizar

---

<a id="sistema-de-logros"></a>
## 🎖️ Sistema de logros

Más de **50 logros** clasificados por categoría, otorgados automáticamente desde los servicios.

### Categorías

- `ESTRATEGIA`
- `TORNEOS`
- `CONTRA_BOTS`
- `PARTIDAS_SIMPLES`
- `ACCIONES_ESPECIALES`
- `EQUIPO`

### Ejemplos de logros

| Nombre | Categoría | Condición |
|--------|-----------|-----------|
| All-In Maniaco | ESTRATEGIA | Hacer All-In 50 veces |
| Bluff Maestro | ESTRATEGIA | Hacer farol en Flop y ganar |
| Sin Fichas | ESTRATEGIA | Quedarse sin fichas globales |
| Superviviente | ESTRATEGIA | Ganar con <5% de fichas |
| Comeback | ACCIONES_ESPECIALES | Ganar mano con <10% de fichas iniciales |
| Derrotador de Máquinas | CONTRA_BOTS | Ganar 10 partidas contra bots |
| Victoria Privada | PARTIDAS_SIMPLES | Ganar una partida privada |
| Jugador Rico | ACCIONES_ESPECIALES | Alcanzar 100K fichas globales |
| Millonario | ACCIONES_ESPECIALES | Alcanzar 1M fichas globales |
| Subidón | ACCIONES_ESPECIALES | Ganar 20K fichas en una partida |
| Clasificado Pro | TORNEOS | Clasificarse en un torneo |
| Jugador en equipo | EQUIPO | Participar en torneo por equipos |
| Campeón por Equipos | EQUIPO | Ganar torneo por equipos |
| Equipo o familia? | EQUIPO | Ganar 3 torneos con mismo equipo |
| Arrasador en Equipo | EQUIPO | Ganar 3 torneos con cualquier equipo |
| Capitán Estratégico | EQUIPO | Ser capitán y ganar |
| Todos a una | EQUIPO | Todo el equipo clasifica a final |

### Otorgamiento

Los logros se otorgan desde servicios mediante:

```java
logroService.otorgarLogroSiNoTiene(userId, "NOMBRE_LOGRO");
```

Iconos asociados en `/files/images/logros/`

---

<a id="moderacion-y-sanciones"></a>
## 🛡️ Moderación y sanciones

### Sistema automático

Las sanciones se sincronizan automáticamente con flags del usuario:

- `PROHIBICION_CHAT` activa → `user.chatBloqueado = true`
- `BLOQUEO_CUENTA` / `SUSPENSION_*` activa → `user.bloqueado = true`

### Job de expiración

`SancionExpiryJob` ejecuta cada minuto:
- Desactiva sanciones caducadas
- Recalcula flags `chatBloqueado` / `bloqueado`
- Solo mantiene activos si hay otras sanciones del mismo tipo

### Tipos de motivos

- `COMPORTAMIENTO_TOXICO`
- `COMPORTAMIENTO_TOXICO_EN_TORNEOS`
- `INACTIVIDAD_EN_PARTIDAS`
- `TRAMPA_DETECTADA`
- `ABUSO_DE_SISTEMA`

---

## 📊 Estadísticas y ranking

### Métricas por usuario

- Manos jugadas
- Manos ganadas
- Fichas ganadas totales
- All-in realizados
- Faroles exitosos
- Torneos ganados
- Puntos de torneo
- Mejores posiciones

### Rankings disponibles

- Ranking global por puntos
- Ranking mensual
- Ranking por fichas ganadas
- Ranking de equipos (global, anual, mensual)

### Progreso mensual

El sistema registra automáticamente el progreso mensual de cada usuario para análisis temporal.

---

<a id="modo-espectador"></a>
## 👁️ Modo espectador

### Funcionalidades

- Observar partidas sin participar
- Ver pot, apuestas públicas y acciones
- Acceso al chat público
- **No** se revelan cartas privadas de jugadores
- Sin límite de espectadores por mesa

### Casos de uso

- Streamers o torneos públicos
- Amigos observando partidas privadas
- Análisis de estrategias

---

## 💾 Datos y persistencia

### Entidades principales

#### User
- `fichas`: Saldo global del usuario
- `bloqueado`: Flag de cuenta bloqueada
- `chatBloqueado`: Flag de chat bloqueado
- `esIA`: Marca si es un bot
- `nivelBot`: Dificultad del bot
- `estiloBot`: Estilo de juego del bot

#### Mesa
- `pot`: Bote total acumulado
- `fase`: Fase actual (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN)
- `esPrivada`: Indica si es mesa privada
- `codigoAcceso`: Código para mesas privadas
- `cartasComunitarias`: Cartas reveladas en la mesa

#### UserMesa
- `fichasEnMesa`: Fichas activas en la mesa
- `totalApostado`: Total apostado en la partida actual
- `activo`: Si el jugador sigue en la mano
- `esSB/esBB/esDealer`: Roles asignados

#### Turno
- `activo`: Si el turno está activo
- `fechaInicio`: Timestamp de inicio
- `accionRealizada`: Acción ejecutada

#### HistorialMano
- `cartasPrivadas`: Cartas del jugador (JSON)
- `manoFinal`: Descripción de la mano ganadora
- `fase`: Fase en que se decidió
- `fichasGanadas`: Cantidad ganada
- `fechaPartida`: Timestamp

#### AccionPartida
- `tipoAccion`: FOLD, CHECK, CALL, RAISE, ALL_IN
- `cantidad`: Monto apostado
- `timestamp`: Momento de la acción

#### Sancion
- `activo`: Si la sanción está vigente
- `tipo`: Tipo de sanción
- `motivo`: Razón de la sanción
- `fechaInicio` / `fechaFin`: Período de vigencia
- `descripcion`: Detalles adicionales

#### LogroUsuario
- `fechaObtencion`: Timestamp de concesión
- Relación ManyToOne con `User` y `Logro`

### Sincronización

- `User.fichas` se actualiza solo al finalizar partidas no privadas
- `UserMesa.fichasEnMesa` es independiente y se elimina al salir
- `HistorialMano` registra cada showdown para estadísticas
- `AccionPartida` permite reconstruir partidas completas

---

## ⏰ Jobs programados

### SancionExpiryJob

```java
@Scheduled(fixedDelayString = "${jobs.sanciones.delay:60000}")
```

**Funciones:**
1. Desactiva sanciones caducadas (`fechaFin` pasada)
2. Recalcula flags del usuario:
  - `chatBloqueado`: false si no hay `PROHIBICION_CHAT` activas
  - `bloqueado`: false si no hay bloqueos/suspensiones activas
3. Log de sanciones procesadas

### TorneoScheduler

```java
@Scheduled(fixedRate = 300000) // Cada 5 minutos
```

**Funciones:**
1. Inicia torneos pendientes cuya `fechaInicio` ha llegado
2. Agrupa jugadores en mesas
3. Asigna ciegas iniciales
4. Cambia estado a `EN_CURSO`

---

## 🧪 Pruebas con Postman

### Colecciones disponibles

1. **Auth & User**
  - Register y Login (guardan `{{token}}` automáticamente)
  - `/api/auth/me`
  - `/api/user/me`
  - `/api/user/me/change-password`

2. **Mesas y Turnos**
  - Crear mesa
  - Unirse a mesa
  - Iniciar turnos
  - Realizar acciones
  - Avanzar fases

3. **Mesas Privadas**
  - Crear mesa privada
  - Unirse con código
  - Añadir bots

4. **Torneos**
  - CRUD de torneos
  - Inscripción
  - Rankings
  - Historial

5. **Equipos**
  - Crear equipo
  - Gestionar miembros
  - Rankings
  - Estadísticas

6. **Logros**
  - Ver catálogo
  - Otorgar logro
  - Ver logros de usuario

7. **Sanciones**
  - Aplicar sanción (admin)
  - Crear sanción (usuario)
  - Listar sanciones
  - Desactivar sanción

8. **Modo Espectador**
  - Unirse como espectador
  - Salir
  - Ver datos de mesa

### Variables de entorno recomendadas

```json
{
  "baseURL": "http://localhost:8080",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Flujo de prueba completo

1. **Registro y autenticación**
   ```http
   POST {{baseURL}}/api/auth/register
   POST {{baseURL}}/api/auth/login
   ```

2. **Crear mesa privada**
   ```http
   POST {{baseURL}}/api/mesas/privadas/crear
   ```

3. **Añadir bots**
   ```http
   POST {{baseURL}}/api/mesas/privadas/CODIGO123/add-bot
   ```

4. **Unirse a mesa**
   ```http
   POST {{baseURL}}/api/mesas/privadas/unirse
   ```

5. **Iniciar partida**
   ```http
   POST {{baseURL}}/api/turnos/iniciar/1
   ```

6. **Realizar acciones**
   ```http
   POST {{baseURL}}/api/turnos/accion/1?accion=RAISE&cantidad=100
   ```

7. **Verificar logros**
   ```http
   GET {{baseURL}}/api/logros/usuario/1
   ```

---

## 🚀 Consideraciones de despliegue

### Plataformas recomendadas

- **Backend:** Render, Railway, Heroku, AWS Elastic Beanstalk
- **Base de datos:** Amazon RDS, PlanetScale, Railway PostgreSQL/MySQL
- **WebSocket:** Asegurar soporte de conexiones persistentes

### Variables de entorno en producción

```properties
# Base de datos
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS
cors.allowed.origins=${FRONTEND_URL}

# Jobs
jobs.sanciones.delay=${SANCION_JOB_DELAY:60000}
```

### Seguridad en producción

- ✅ Cambiar `app.jwt.secret` por valor seguro aleatorio
- ✅ Usar HTTPS obligatorio
- ✅ Configurar CORS restrictivo
- ✅ Habilitar rate limiting
- ✅ Logs de auditoría para acciones sensibles
- ✅ Backup automático de base de datos

### Optimizaciones

- Índices en tablas frecuentes (`User`, `Mesa`, `Turno`)
- Connection pooling (HikariCP)
- Caché para rankings y estadísticas (Redis)
- Compresión de respuestas HTTP
- Paginación en listados grandes

### Monitoreo

- Spring Boot Actuator para health checks
- Métricas con Micrometer + Prometheus
- Logs centralizados (ELK Stack / CloudWatch)
- Alertas para errores críticos

### Docker Compose (desarrollo)

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: pokerdb
      MYSQL_USER: user
      MYSQL_PASSWORD: pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    build: ./poker-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/pokerdb
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: pass
    depends_on:
      - mysql

volumes:
  mysql_data:
```

---

<a id="roadmap"></a>
## 🗺️ Roadmap

### ✅ Funcionalidades completadas

- ✅ Póker multijugador completo (Texas Hold'em)
- ✅ Evaluación de manos con desempates
- ✅ Reparto proporcional de pot (side pots)
- ✅ IA realista: bots con bluff, slowplay, estrategia
- ✅ Chat simulado por bots
- ✅ Mesas privadas con fichas temporales
- ✅ Modo espectador completo
- ✅ Registro de historial y acciones
- ✅ Estadísticas por jugador
- ✅ Sistema de torneos (individual y por equipos)
- ✅ Sistema de logros automático (+50 logros)
- ✅ Moderación con sanciones automáticas
- ✅ Rondas automáticas en torneos
- ✅ Reparto de premios por ranking
- ✅ Avance automático entre fases de torneo
- ✅ WebSocket para eventos en tiempo real

### 🔄 En desarrollo

- 🔄 Chat in-game entre jugadores reales
- 🔄 Sistema de amigos y mensajería
- 🔄 Notificaciones push

### 📅 Próximas funcionalidades

#### Corto plazo
- [ ] Reloj de turnos visual en UI
- [ ] Paginación en listados grandes (historial, sanciones, torneos)
- [ ] Filtros avanzados en rankings
- [ ] Sistema de replay de partidas
- [ ] Perfil público enriquecido con gráficos

#### Medio plazo
- [ ] Sistema de recompensas diarias
- [ ] Torneos Sit & Go automáticos
- [ ] Mesas de cash game persistentes
- [ ] Sistema de niveles y experiencia
- [ ] Tienda virtual con avatares/items

#### Largo plazo
- [ ] Variantes de póker (Omaha, Seven Card Stud...)
- [ ] Sistema de apuestas paralelas
- [ ] Integración con pasarelas de pago
- [ ] App móvil nativa (Flutter)
- [ ] Modo torneo en vivo con streaming
- [ ] Sistema de afiliados y referidos

### 🔧 Mejoras técnicas planificadas

- [ ] Migración a arquitectura de microservicios
- [ ] Implementar caché distribuida (Redis)
- [ ] Métricas y observabilidad (Prometheus + Grafana)
- [ ] CI/CD completo (GitHub Actions)
- [ ] Tests de integración completos
- [ ] Documentación OpenAPI/Swagger
- [ ] Rate limiting por IP/usuario
- [ ] Compresión de WebSocket

---

## 📚 Recursos adicionales

### Documentación de referencia

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [WebSocket with Spring](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [JPA/Hibernate Guide](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

### Reglas de Texas Hold'em

- [PokerStars Rules](https://www.pokerstars.com/poker/games/texas-holdem/)
- [Evaluación de manos](https://www.cardschat.com/poker-hands/)

### Contribuir al proyecto

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit de cambios (`git commit -am 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abrir Pull Request

### Reportar bugs

Abre un Issue en GitHub incluyendo:
- Descripción del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Logs relevantes
- Entorno (OS, Java version, etc.)

---

## 📄 Licencia

**MIT License**

Copyright (c) 2025 Marc Martín

Se concede permiso, de forma gratuita, a cualquier persona que obtenga una copia de este software y archivos de documentación asociados (el "Software"), para utilizar el Software sin restricciones, incluyendo sin limitación los derechos a usar, copiar, modificar, fusionar, publicar, distribuir, sublicenciar, y/o vender copias del Software, y a permitir a las personas a las que se les proporcione el Software a hacer lo mismo, sujeto a las siguientes condiciones:

El aviso de copyright anterior y este aviso de permiso se incluirán en todas las copias o porciones sustanciales del Software.

EL SOFTWARE SE PROPORCIONA "TAL CUAL", SIN GARANTÍA DE NINGÚN TIPO, EXPRESA O IMPLÍCITA, INCLUYENDO PERO NO LIMITADO A GARANTÍAS DE COMERCIALIZACIÓN, IDONEIDAD PARA UN PROPÓSITO PARTICULAR Y NO INFRACCIÓN. EN NINGÚN CASO LOS AUTORES O TITULARES DEL COPYRIGHT SERÁN RESPONSABLES DE NINGUNA RECLAMACIÓN, DAÑOS U OTRAS RESPONSABILIDADES, YA SEA EN UNA ACCIÓN DE CONTRATO, AGRAVIO O CUALQUIER OTRO MOTIVO, QUE SURJA DE O EN CONEXIÓN CON EL SOFTWARE O EL USO U OTRO TIPO DE ACCIONES EN EL SOFTWARE.

---

## 👨‍💻 Créditos y contacto

**Desarrollador principal:** Marc Martín  
**Twitter/X:** [@marukunai_03](https://x.com/marukunai_03)  
**GitHub:** [Marukunai](https://github.com/Marukunai)  
**Repositorio:** [poker_online](https://github.com/Marukunai/poker_online)

### Agradecimientos

- A la comunidad de Spring Boot por su excelente documentación
- A los testers y usuarios beta
- A mi hermano de otra sangre [Ayman](https://github.com/KaizoIncc) por darme buenas ideas para implementar en el proyecto

---

## 📝 Historial de cambios

### v2.0.0 (30 de septiembre de 2025)
- ✨ Sistema de torneos por equipos
- ✨ Sistema de logros automático (+50 logros)
- ✨ Modo espectador
- ✨ Sistema de sanciones con jobs automáticos
- ✨ Mejoras en bots (chat simulado, estilos de juego)
- 🐛 Correcciones en reparto de side pots
- 📚 Documentación completa unificada

### v1.5.0 (22 de julio de 2025)
- ✨ Mesas privadas con fichas temporales
- ✨ Sistema de bots con IA
- ✨ Sistema de torneos básico
- 🐛 Correcciones en evaluación de manos
- 📊 Sistema de estadísticas

### v1.0.0 (Fecha inicial)
- 🎉 Lanzamiento inicial
- ✅ Lógica completa de Texas Hold'em
- ✅ Autenticación JWT
- ✅ WebSocket para tiempo real
- ✅ Sistema básico de usuarios y mesas

---

## ❓ FAQ (Preguntas frecuentes)

### ¿Puedo usar este proyecto comercialmente?
Sí, bajo licencia MIT puedes usar, modificar y distribuir este software comercialmente, siempre que mantengas el aviso de copyright.

### ¿Cómo reporto un problema de seguridad?
Por favor, **NO** abras un Issue público. Contacta directamente al desarrollador vía GitHub o email privado.

### ¿Puedo añadir nuevas variantes de póker?
¡Absolutamente! El código está diseñado para ser extensible. Recomendamos crear nuevas clases de servicio que hereden de `BasePokerService`.

### ¿Hay límite de jugadores por mesa?
Actualmente el límite es de 8 jugadores por mesa (estándar de Texas Hold'em), pero es configurable en `Mesa.maxJugadores`.

### ¿Los bots pueden jugar en torneos?
No, los bots solo están disponibles en mesas privadas para practicar o jugar con amigos.

### ¿Cómo se calculan los rankings?
Los rankings se basan en múltiples métricas: partidas ganadas, fichas ganadas, puntos de torneo y rendimiento mensual. Cada ranking tiene su propio algoritmo de ordenación.

### ¿Puedo crear un torneo privado?
Sí, los torneos pueden configurarse como privados mediante flags adicionales (funcionalidad en desarrollo).

### ¿Qué pasa si pierdo conexión durante una partida?
El sistema marca al jugador como inactivo y realiza FOLD automático en sus turnos. Al reconectar, puede volver a unirse si la partida sigue activa.

---

**¡Gracias por usar Poker Online Backend!**

Si encuentras útil este proyecto, considera darle una ⭐ en [GitHub](https://github.com/Marukunai/poker_online).

---

*Última actualización: 30 de septiembre de 2025*