# Documentación del Backend - Poker Online ♦️

## 🏠 Introducción

Este proyecto representa el backend de una aplicación web y móvil de póker en línea, diseñado como proyecto de autoaprendizaje y desarrollo. El objetivo es ofrecer una experiencia de póker realista y multijugador, con autenticación segura, gestión de partidas, apuestas, lógica de juego, y una futura interfaz frontend.

---

## 📂 Estructura del proyecto

```
/poker-online
├── backend/                           # Proyecto Spring Boot (Java)
│   ├── controller/                    # Controladores REST
│   ├── service/                       # Lógica de negocio
│   ├── model/                         # Entidades JPA (User, Mesa, Turno...)
│   ├── dto/                           # Objetos de transferencia de datos
│   ├── repository/                    # Repositorios JPA
│   ├── config/                        # Seguridad JWT y configuraciones
│   ├── PokerBackendApplication.java   # Clase principal
│   └── ...
├── docs/                              # Documentación Markdown (GitHub Pages)
│   ├── index.md           
│   ├── backend.md                     # Esta documentación
│   └── frontend.md                    # Documentación para el frontend (aún no implementado)
└── frontend/                          # (Futuro) Frontend React/Flutter
```

---

## 🎯 Tecnologías utilizadas

* **Java 17** + Spring Boot 3
* **JWT** para autenticación segura
* **Hibernate/JPA** para persistencia
* **MySQL** como base de datos
* **WebSocket** para comunicación en tiempo real (turnos, acciones)
* **Postman** para pruebas
* **Spring Security** para roles y protección de rutas
* **Lombok** para simplificar entidades y DTOs

---

# Inicio de aplicativo

Antes de poder usar la aplicación, primero debemos asegurarnos de tener una base de datos como la que definimos en [application.properties](https://github.com/Marukunai/poker_online/blob/b7ef2a663bd3bd4e3464f558dc1fbf55f85a7347/poker-backend/src/main/resources/application.properties).

Si no la tienes creada, puedes ejecutarla desde el mismo archivo [docker-compose.yml](), o bien:

## 1. 🐬 Iniciar la base de datos MySQL

Debes tener un contenedor Docker corriendo con MySQL (versión 8.0) y con la configuración adecuada (nombre de base de datos, usuario, contraseña).

Puedes usar el siguiente comando si no tienes uno creado:

```bash
docker run --name mysql_poker -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=poker -e MYSQL_USER=user -e MYSQL_PASSWORD=pass -p 3306:3306 -d mysql:8.0
```

Acto seguido, y una vez la BD quede bien iniciada (comprobaremos que tenemos un contenedor creado llamado mysql_poker) usando los siguientes comandos:

```bash
docker ps #para comprobar que está creado
```

Debería salir algo así:

```commandline
CONTAINER ID   IMAGE       COMMAND                  CREATED          STATUS          PORTS                               NAMES
............   mysql:8.0   "docker-entrypoint.s…"   .. minutes ago   Up .. minutes   0.0.0.0:3306->3306/tcp, 33060/tcp   mysql_poker
```

## 2. ⚙️ Configurar el proyecto backend

Requisitos previos:

- Tener instalado Java 17+
- Tener instalado Gradle o usar el wrapper (./gradlew)
- Tener un IDE (recomendado: IntelliJ IDEA, VSCode o Eclipse)

### Clonar el repositorio

```bash
git clone https://github.com/Marukunai/poker_online.git
```

Verificar el archivo [application.properties](https://github.com/Marukunai/poker_online/blob/b7ef2a663bd3bd4e3464f558dc1fbf55f85a7347/poker-backend/src/main/resources/application.properties).

Asegúrate de que coincide con la configuración de tu base de datos:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pokerdb
spring.datasource.username=user
spring.datasource.password=jupiter*
spring.jpa.hibernate.ddl-auto=update
```

## 3. 🚀 Ejecutar el backend

- Opción A: Desde línea de comandos
   
```bash
cd poker_online/poker-backend
./gradlew bootRun
```

- Opción B: Desde IntelliJ / Eclipse:

   Importa el proyecto como proyecto Gradle.

   Asegúrate de que se descarguen las dependencias (build.gradle).

   Ejecuta la clase [PokerBackendApplication.java](https://github.com/Marukunai/poker_online/blob/d871505914e044997bb40eae389340226ac6049c/poker-backend/src/main/java/com/pokeronline/PokerBackendApplication.java).

## 4. 📦 Probar que está funcionando

Abre tu navegador y accede a:

```bash
http://localhost:8080/api/mesas
```

Deberías ver una lista de mesas (o un array vacío si no hay mesas creadas aún).

También puedes probar con Postman usando las rutas documentadas en el backend.

## 🔑 Autenticación JWT

Los usuarios se autentican mediante login con correo y contraseña, obteniendo un token JWT. Este token se adjunta en las peticiones posteriores como:

```http
Authorization: Bearer {token}
```

---

## 🎬 Flujo general de una partida de póker

1. 🔑 **Registro/Login**
2. 📍 **Unión a una mesa** (`/api/mesas/unirse/{mesaId}`)
3. ▶️ **Inicio de la partida** (`/api/turnos/iniciar/{mesaId}`)
4. ♦️ **Reparto de cartas privadas** (`BarajaService` lo gestiona)
5. ⏳ **Turnos de acción** (check, fold, call, raise...)
6. 📊 **Avance de fases** (`/api/turnos/fase/{mesaId}/siguiente`)

    * Pre-Flop → Flop → Turn → River → Showdown
7. ⚖️ **Evaluación de manos** (`EvaluadorManoService`)
8. 🌟 **Reparto del pot** (`MesaService`, incluyendo side pots)
9. ⟳ **Inicio de nueva ronda**

---

## 📑 Endpoints destacados (Turnos)

### 🚀 Iniciar turnos

```http
POST /api/turnos/iniciar/{mesaId}
```

* Inicializa la baraja, reparte cartas y prepara la primera ronda

### ⏱️ Ver turno actual

```http
GET /api/turnos/actual/{mesaId}
```

* Devuelve el `Turno` activo actualmente

### ➡️ Avanzar turno

```http
POST /api/turnos/avanzar/{mesaId}
```

* Desactiva el turno actual y activa el siguiente disponible

### 🏛️ Realizar acción

```http
POST /api/turnos/accion/{mesaId}?accion=RAISE&cantidad=50
Authorization: Bearer {token}
```

* Acciones: `FOLD`, `CHECK`, `CALL`, `RAISE`, `ALL_IN`
* Valida fichas, apuestas mínimas, y avanza el turno

### 🔄 Avanzar fase

```http
POST /api/turnos/fase/{mesaId}/siguiente
```

* Cambia a la siguiente fase y reparte cartas comunitarias si corresponde

---

## 🏆 Roles en mesa

* Dealer
* Small Blind
* Big Blind
* Jugadores normales

> Actualmente los roles se asignan al iniciar partida y podrán rotar en futuras versiones.

---

## ⚡ Control de fichas

* `User.fichas`: fichas globales del usuario
* `UserMesa.fichasEnMesa`: fichas con las que entra a la mesa
* `UserMesa.totalApostado`: lo que ha puesto en la partida actual
* `Mesa.pot`: bote total en juego

---

### 🔒 Mesas privadas

Ahora es posible crear mesas privadas con:

- Código de acceso personalizado (para invitar amigos).
- Fichas **temporales**, no afectando el saldo global del usuario.
- Soporte para hasta 8 jugadores.

#### Endpoints:

```http
POST /api/mesas/privadas/crear
```

Parámetros: nombre, maxJugadores, código, fichasTemporales, smallBlind, bigBlind

```http
POST /api/mesas/privadas/unirse
```

Parámetros: email, códigoAcceso, fichasSolicitadas

```http
POST /api/mesas/privadas/{codigo}/add-bot
```

Añade un bot a la mesa privada.

### ⚠️ Reglas de fichas temporales

- Máximo: 10 millones por jugador.
- No se guardan en `User.fichas`.
- Se eliminan al salir de la mesa.

---

## 🔄 Mejoras internas

- Se impide unirse a varias mesas a la vez (se desconecta de la anterior).
- `DataLoader` más completo con 6 usuarios, 3 mesas realistas, fichas variadas.
- IA ignorada en lógica de ranking, fichas, pot.
- Control reforzado en uniones a mesa (si ya está unido, no lo vuelve a hacer).

---

## 🧠 Jugadores controlados por IA (bots)

Hemos implementado soporte para añadir bots en partidas privadas, los cuales cuentan con un comportamiento avanzado y realista, incluyendo decisiones estratégicas, estilo de juego y simulación de chat.

- Los bots se representan como `User` con `esIA = true`.
- Se crean dinámicamente con nombres únicos como `CPU-32`.
- Sólo pueden añadirse en mesas privadas.
- Ignorados al calcular fichas globales (`User.fichas`).

- Los bots ahora tienen un comportamiento avanzado y realista, incluyendo decisiones estratégicas, estilo de juego, y simulación de chat.

### 🔧 Características principales

- Representados como objetos `User` con `esIA = true`.
- Solo pueden añadirse a **mesas privadas**.
- Actúan automáticamente cuando les llega el turno, con un **retardo de 10-15 segundos** simulando "pensamiento".

```java
User bot = User.builder()
  .email("cpu...@bot.com")
  .username("CPU-XX")
  .esIA(true)
  .nivelBot(DificultadBot.NORMAL) // FACIL, NORMAL, DIFICIL
  .estiloBot(EstiloBot.AGRESIVO)  // AGRESIVO, CONSERVADOR, LOOSE, TIGHT, DEFAULT
  .build();
```

### 🎚️ Dificultad y estilo de juego

Los bots toman decisiones en base a su nivel de dificultad (`FACIL`, `NORMAL`, `DIFICIL`) y su estilo (`AGRESIVO`, `CONSERVADOR`, `LOOSE`, `TIGHT`, `DEFAULT`):

| Nivel     | Bluff | Slowplay | Evaluación contextual        |
|-----------|-------|----------|------------------------------|
| FACIL     | ❌    | ❌       | Decisiones simples           |
| NORMAL    | ⚠️     | ❌       | Usa draws y conectadas       |
| DIFICIL   | ✅    | ✅       | Analiza fuerza + faroles     |

| Estilo       | Agresividad | Comportamiento                        |
|--------------|-------------|--------------------------------------|
| AGRESIVO     | Alto (1.4x) | Muchos raise y all-in                |
| CONSERVADOR  | Bajo (0.7x) | Cauto, se retira con facilidad       |
| LOOSE        | Medio (1.2x)| Juega muchas manos                   |
| TIGHT        | Medio (0.8x)| Solo juega manos fuertes             |
| DEFAULT      | 1.0x        | Equilibrado                          |

Los bots ajustan sus decisiones con una mezcla de:

- **Fuerza de la mano evaluada**
- **Conectividad y suited**
- **Flush/Straight draw**
- **Probabilidad de bluff o slowplay según dificultad**

### 💬 Frases simuladas por WebSocket

Cada vez que un bot actúa, puede enviar una frase simulada en el chat, según su acción y estilo:

```json
{
  "jugador": "CPU-42",
  "mensaje": "¡A ver si aguantas esta!"
}
```

Estas frases se gestionan mediante el enum `FrasesBotChat.java`.

---

### 📛 Lógica de control y reglas

- Solo el **creador de la mesa** puede añadir bots.
- El número de bots se limita por el `maxJugadores` de la mesa.
    - Si hay 6 plazas y ya hay 4 humanos, sólo se pueden añadir 2 bots.
    - Si un humano se une y la mesa está llena, se elimina automáticamente un bot.
- Los bots **no afectan** el saldo global (`User.fichas`) y usan **fichas temporales** (`UserMesa.fichasEnMesa`).
- Bots eliminados si abandonan mesa o se reemplazan por humanos.

---

## 🧪 Testing sugerido

- Crear mesa privada con contraseña.
- Añadir 1–2 jugadores reales.
- Añadir bots.
- Iniciar partida y observar comportamiento.
- Verificar que el `User.fichas` no cambia tras salir.

---

## 🧾 Notas finales

Estas funcionalidades buscan permitir un entorno de juego más flexible y justo entre amigos o entornos cerrados, sin comprometer la economía interna del sistema.

Puedes consultar la evolución de esta lógica dentro del paquete `service/`, especialmente en:

- `MesaPrivadaService`
- `UserMesaService`
- `MesaService`
- `TurnoService`

--- 

## FUNCIONALIDADES YA IMPLEMENTADAS

- ✔️ Lógica completa de poker por rondas: Pre-flop, Flop, Turn, River.

- ✔️ Evaluación real de manos (EvaluadorManoService) incluyendo desempates.

- ✔️ Reparto proporcional de bote (incluyendo empates).

- ✔️ Bots con IA realista:
  - Dificultades (FÁCIL, NORMAL, DIFÍCIL).
  - Estilos de juego (AGRESIVO, CONSERVADOR, LOOSE, TIGHT, DEFAULT). 
  - Bluff, slowplay, chat simulado.

- ✔️ Lógica de decisiones de bot según mano, fase y contexto.

- ✔️ Chat del bot vía WebSocket (FrasesBotChat, integración en BotService).

- ✔️ Restricciones en número de bots (y reemplazo si mesa llena).

- ✔️ Registro de acciones (AccionPartida).

- ✔️ Control de fichas globales vs. fichas en mesa.

- ✔️ WebSocket en todas las acciones relevantes.

- ✔️ Control total de las partidas privadas: acceso, uniones, bots, fichas temporales.

---

## 👁️ Modo espectador

- Usuarios pueden observar partidas sin participar
- No ven cartas privadas
- Observan pot, apuestas, acciones, chat

### Endpoints

```
POST   /api/mesa/espectadores/{mesaId}/unirse
DELETE /api/mesa/espectadores/{mesaId}/salir
GET    /api/mesa/espectadores/{mesaId}
GET    /api/mesa/espectadores/{mesaId}/datos
```

---

## 🧾 Registro de acciones e historial

- Cada acción (check, raise, fold...) se guarda (`AccionPartida`)
- Cada showdown se registra por jugador (`HistorialMano`):
  - Cartas privadas
  - Mano final
  - Fase
  - Fichas ganadas
  - Timestamp

Permite reconstruir o analizar partidas completas.

---

## 🧪 Pruebas (Postman)

- Crear torneo
- Inscribirse (token necesario)
- Añadir participantes
- Ver ranking
- Ver historial
- Añadir bots
- Unirse como espectador

---

## ✅ Funcionalidades completadas

- ✅ Poker multijugador completo
- ✅ Evaluación de manos con desempates
- ✅ Reparto proporcional de pot (side pots)
- ✅ IA realista: bots con bluff, slowplay, estrategia
- ✅ Chat simulado por bots
- ✅ Mesas privadas con fichas temporales
- ✅ Modo espectador completo
- ✅ Registro de historial y acciones
- ✅ Estadísticas por jugador
- ✅ Sistema de torneos básico
- ✅ Rondas automáticas en torneos
- ✅ Reparto de premios por ranking
- ✅ Avance automático entre fases de torneo

--- 

## 🚀 Sistema de torneos

* Torneos con buy-in, premios y rondas automáticas
* Soporte para torneos por equipos
* Sistema de eliminación directa o ranking por puntos
* Ciegas crecientes por nivel (blind levels)
* Integración con WebSocket para eventos en tiempo real

### **Endpoints REST** (TorneoController):

```
POST   /api/torneos
GET    /api/torneos
GET    /api/torneos/{id}
POST   /api/torneos/{id}/inscribirse
GET    /api/torneos/{id}/ranking
POST   /api/torneos/{id}/empezar
```

**Entidades:** `Torneo`, `ParticipanteTorneo`, `TorneoMesa`, `BlindLevel`

---

### 👥 Torneos por equipos

* Equipos con nombre, capitán y miembros
* Participación por equipos en torneos
* Ranking por rendimiento colectivo
* Logros específicos para torneos por equipos

**Endpoints REST** (EquipoTorneoController):

```
POST   /api/equipos/torneo/{torneoId}/crear
POST   /api/equipos/{equipoId}/agregar-miembro
GET    /api/equipos/{equipoId}
GET    /api/equipos/torneo/{torneoId}/ranking
```

**Entidades:** `EquipoTorneo`, `MiembroEquipoTorneo`

---

## 🏆 Sistema de logros (automático)

Se han implementado más de 50 logros dedicados a los jugadores, para que vayan consiguiendo dichos logros y así incitarlos a conseguirlos todos. 

* Logros otorgados desde servicios (`MesaService`, `TurnoService`, `TorneoService`, etc.)
* Clasificados por categoría: `ESTRATEGIA`, `TORNEOS`, `CONTRA_BOTS`, `PARTIDAS_SIMPLES`, `ACCIONES_ESPECIALES`, `EQUIPO`
* Iconos asociados en `/files/images/logros/`

**Ejemplos de logros implementados:**

| Nombre                 | Categoría            | Condición                                                  |
| ---------------------- | -------------------- | ---------------------------------------------------------- |
| All-In Maniaco         | ESTRATEGIA           | Hacer All-In 50 veces (TurnoService)                       |
| Bluff Maestro          | ESTRATEGIA           | Hacer farol en Flop y ganar (TurnoService + MesaService)   |
| Sin Fichas             | ESTRATEGIA           | Quedarse sin fichas globales (TurnoService)                |
| Superviviente          | ESTRATEGIA           | Ganar con <5% fichas (MesaService)                         |
| Comeback               | ACCIONES\_ESPECIALES | Ganar mano comenzando con <10% de fichas (MesaService)     |
| Derrotador de Máquinas | CONTRA\_BOTS         | Ganar 10 partidas contra bots (MesaService)                |
| Victoria Privada       | PARTIDAS\_SIMPLES    | Ganar una partida privada (MesaService)                    |
| Jugador Rico           | ACCIONES\_ESPECIALES | Alcanzar 100K fichas globales (MesaService)                |
| Millonario             | ACCIONES\_ESPECIALES | Alcanzar 1M fichas globales (MesaService)                  |
| Subidón                | ACCIONES\_ESPECIALES | Ganar 20K fichas en una sola partida (MesaService)         |
| Clasificado Pro        | TORNEOS              | Clasificarse en torneo (TorneoService)                     |
| Jugador en equipo      | EQUIPO               | Participar en torneo por equipos (EquipoTorneoService)     |
| Campeón por Equipos    | EQUIPO               | Ganar torneo por equipos (EquipoTorneoService)             |
| Equipo o familia?      | EQUIPO               | Ganar 3 torneos con mismo equipo (EquipoTorneoService)     |
| Arrasador en Equipo    | EQUIPO               | Ganar 3 torneos con cualquier equipo (EquipoTorneoService) |
| Capitán Estratégico    | EQUIPO               | Ser capitán y ganar (EquipoTorneoService)                  |
| Todos a una            | EQUIPO               | Todo el equipo clasifica a final (EquipoTorneoService)     |

> Los logros se gestionan desde `LogroUsuarioService` y se otorgan mediante `logroService.otorgarLogroSiNoTiene(...)`

---

## 🔢 Estadísticas y ranking

* Estadísticas por usuario:

  * manosJugadas, manosGanadas, fichasGanadas, all-in realizados, faroles
  
* Estadísticas por torneo:

  * torneos ganados, puntos, mejores posiciones, progreso mensual
  
* Ranking global y mensual por puntos y fichas ganadas

**Endpoints:**

```
GET /api/estadisticas/usuario/{id}
GET /api/estadisticas/ranking/global
GET /api/estadisticas/ranking/mensual
```

---

## 🏃️ Avance automático en torneos

* Programador `@Scheduled` que inicia torneos cuando llega la fecha (`TorneoScheduler`)
* Agrupa jugadores en `TorneoMesa` por rondas
* Avanza de fase cuando queda 1 jugador por mesa
* Asigna premios al finalizar

> Lógica gestionada desde `TorneoService` y `TorneoMesaService`

---

## 🔗 WebSocket (eventos en tiempo real)

* Acciones como turno, fase, all-in, showdown y resultado final se notifican por WebSocket
* Compatible con modo espectador
* Frases de bots simuladas como mensajes de chat

---

## 🔧 Mejoras internas respecto a la última versión

* Control completo de bots en mesas privadas
* Separación clara entre fichas globales y fichas en mesa
* Integración de logros y estadísticas sin afectar rendimiento
* Control de reemplazo de bots si la mesa se llena

---

## 📅 Registro de historial

Cada showdown guarda información en `HistorialMano`:

* Usuario, cartas, mano final, fichas ganadas, fecha
* Base para progreso mensual y paneles estadísticos

---

## 🔒 Seguridad y autenticación

* JWT con filtros personalizados (`JwtAuthenticationFilter`)
* Roles: USER, ADMIN
* Endpoints protegidos por `@PreAuthorize` si es necesario

---

## 🏆 Próximas funcionalidades

- [ ] Reloj de turnos visual (UI) (frontend)
- [ ] Sistema de chat in-game
- [ ] Integración UI con estadísticas (frontend)
- [ ] Perfil público de jugador con ranking
- [ ] Sala de espera en torneos 


## 💼 Consideraciones de despliegue

* Recomendado para hosting: **Render**, **Railway**, **Heroku (Java)**
* DB externa: PlanetScale o Amazon RDS
* Variables de entorno para `JWT_SECRET`, `DB_URL`, etc.

Actualmente, se utiliza una BD personal para pruebas. Una vez esté el proyecto terminado, se probará de trasladarlo a una BD global para hosting.

---

## 📚 Créditos y colaboración

Este proyecto ha sido desarrollado por [**Marc Martín**](https://x.com/marukunai_03).

> Si deseas colaborar o extender la lógica, puedes abrir un `Pull Request` o contactar vía GitHub Issues.

---

## 🔖 Licencia

MIT License. Libre uso con crédito al autor original.

----


## 📅 Última actualización

2025-07-22 14:01:00