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
cd poker_online/poker-backend
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

## 🚀 Nuevas funcionalidades implementadas

### 🧠 Jugadores controlados por IA (bots)

Se ha añadido soporte para añadir bots en partidas privadas:

- Los bots se representan como `User` con `esIA = true`.
- Se crean dinámicamente con nombres únicos como `CPU-32`.
- Sólo pueden añadirse en mesas privadas.
- Ignorados al calcular fichas globales (`User.fichas`).

```java
User bot = User.builder()
  .email("cpu...@bot.com")
  .username("CPU-XX")
  .esIA(true)
  .build();
```

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

## 📦 Pruebas y depuración

Los endpoints nuevos pueden probarse vía Postman:

- Crear mesa privada.
- Unirse con fichas temporales.
- Añadir CPU bots.
- Validar que no afecta al saldo global.

---

## 🏆 Próximas funcionalidades

- [ ] Reloj de turnos visual (UI)
- [ ] Modo espectador (join sin jugar)
- [ ] Sistema de chat in-game
- [ ] Clasificación de jugadores (ranking, torneos)
- [ ] IA con decisiones de juego (fold, call, raise)

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

## 🪧 Tareas futuras (Backend)

* [ ] Rotación de roles por ronda
* [ ] Reingreso a mesas en curso (si se reconecta)
* [ ] Lógica de reloj de turnos más realista
* [ ] Soporte a partidas privadas
* [ ] Modo espectador

---

## 📅 Historial de manos

Cada showdown se guarda por jugador:

* Cartas que tenía
* Mano ganadora
* Fase final
* Fecha y hora
* Fichas ganadas o perdidas

Esto permite un análisis post-partida o ranking general.

---

## 🔧 WebSocket (eventos en tiempo real)

Eventos enviados desde servidor a mesa:

* `turno`: nuevo jugador en turno
* `accion`: acción tomada
* `fase`: cambio de fase
* `ganador`: resultado final

---

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