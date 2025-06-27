# Documentación del Backend - Poker Online ♦️

## 🏠 Introducción

Este proyecto representa el backend de una aplicación web y móvil de póker en línea, diseñado como proyecto final de grado. El objetivo es ofrecer una experiencia de póker realista y multijugador, con autenticación segura, gestión de partidas, apuestas, lógica de juego, y una futura interfaz frontend.

---

## 📂 Estructura del proyecto

```
/poker-online
├── backend/               # Proyecto Spring Boot (Java)
│   ├── controller/        # Controladores REST
│   ├── service/           # Lógica de negocio
│   ├── model/             # Entidades JPA (User, Mesa, Turno...)
│   ├── dto/               # Objetos de transferencia de datos
│   ├── repository/        # Repositorios JPA
│   ├── config/            # Seguridad JWT y configuraciones
│   ├── PokerOnlineApp.java# Clase principal
│   └── ...
├── docs/                  # Documentación Markdown (GitHub Pages)
│   └── backend.md       # Esta documentación
└── frontend/              # (Futuro) Frontend React/Flutter
```

---

## 🎯 Tecnologías utilizadas

* **Java 21** + Spring Boot 3
* **JWT** para autenticación segura
* **Hibernate/JPA** para persistencia
* **MySQL** como base de datos
* **WebSocket** para comunicación en tiempo real (turnos, acciones)
* **Postman** para pruebas

---

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

---

## 📚 Créditos y colaboración

Este proyecto ha sido desarrollado por \[Tu Nombre] como trabajo de final de grado.

> Si deseas colaborar o extender la lógica, puedes abrir un `Pull Request` o contactar vía GitHub Issues.

---

## 🔖 Licencia

MIT License. Libre uso con crédito al autor original.