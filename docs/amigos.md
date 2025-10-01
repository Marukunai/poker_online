# Sistema de Amigos - Poker Online 👥

# 📋 Índice

1. [Introducción](#introduccion)
2. [Arquitectura del sistema](#arquitectura-del-sistema)
3. [Modelo de datos](#modelo-de-datos)
4. [Funcionalidades principales](#funcionalidades-principales)
5. [Endpoints REST](#endpoints-rest)
6. [WebSocket: eventos en tiempo real](#websocket-eventos-en-tiempo-real)
7. [Sistema de chat privado](#sistema-de-chat-privado)
8. [Transferencia de fichas](#transferencia-de-fichas)
9. [Invitaciones a partidas](#invitaciones-a-partidas)
10. [Sistema de presencia](#sistema-de-presencia)
11. [Notificaciones](#notificaciones)
12. [Privacidad y configuración](#privacidad-y-configuracion)
13. [Límites y restricciones](#limites-y-restricciones)
14. [Implementación técnica](#implementacion-tecnica)

---

<a id="introduccion"></a>
## 🎯 Introducción

El sistema de amigos permite a los jugadores:

- ✅ Enviar y aceptar solicitudes de amistad
- ✅ Ver amigos en línea y su estado actual
- ✅ Unirse a partidas de amigos (como jugador o espectador)
- ✅ Chat privado en tiempo real con mensajes, audios, GIFs y stickers
- ✅ Transferir fichas entre amigos
- ✅ Crear salas de chat grupales
- ✅ Invitar amigos directamente a mesas privadas
- ✅ Ver estadísticas y logros de amigos
- ✅ Recibir notificaciones de actividad de amigos
- ✅ Sistema de favoritos dentro de la lista de amigos

---

<a id="arquitectura-del-sistema"></a>
## 🏗️ Arquitectura del sistema

```
/poker-backend
├── amigos/
│   ├── controller/
│   │   ├── AmigosController.java
│   │   ├── ChatPrivadoController.java
│   │   └── TransferenciaFichasController.java
│   ├── service/
│   │   ├── AmigosService.java
│   │   ├── ChatPrivadoService.java
│   │   ├── PresenciaService.java
│   │   ├── TransferenciaFichasService.java
│   │   └── InvitacionPartidaService.java
│   ├── repository/
│   │   ├── AmistadRepository.java
│   │   ├── SolicitudAmistadRepository.java
│   │   ├── MensajePrivadoRepository.java
│   │   ├── TransferenciaFichasRepository.java
│   │   └── InvitacionPartidaRepository.java
│   ├── model/
│   │   ├── Amistad.java
│   │   ├── SolicitudAmistad.java
│   │   ├── MensajePrivado.java
│   │   ├── TransferenciaFichas.java
│   │   ├── InvitacionPartida.java
│   │   └── ConfiguracionPrivacidad.java
│   ├── dto/
│   │   ├── AmigoDTO.java
│   │   ├── SolicitudAmistadDTO.java
│   │   ├── MensajePrivadoDTO.java
│   │   ├── TransferenciaFichasDTO.java
│   │   └── InvitacionPartidaDTO.java
│   └── websocket/
│       └── AmigosWebSocketHandler.java
```

---

<a id="modelo-de-datos"></a>
## 💾 Modelo de datos

### Entidad: Amistad

```java
@Entity
@Table(name = "amistades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amistad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario1_id", nullable = false)
    private User usuario1;
    
    @ManyToOne
    @JoinColumn(name = "usuario2_id", nullable = false)
    private User usuario2;
    
    @Column(nullable = false)
    private LocalDateTime fechaAmistad;
    
    @Column(nullable = false)
    private Boolean esFavorito1 = false;  // usuario1 marcó como favorito
    
    @Column(nullable = false)
    private Boolean esFavorito2 = false;  // usuario2 marcó como favorito
    
    @Column(nullable = false)
    private Boolean notificacionesActivas1 = true;
    
    @Column(nullable = false)
    private Boolean notificacionesActivas2 = true;
    
    private String alias1;  // Alias personalizado que usuario1 le pone a usuario2
    private String alias2;  // Alias personalizado que usuario2 le pone a usuario1
}
```

### Entidad: SolicitudAmistad

```java
@Entity
@Table(name = "solicitudes_amistad")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAmistad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;
    
    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaEnvio;
    
    private LocalDateTime fechaRespuesta;
    
    private String mensaje;  // Mensaje opcional al enviar solicitud
}

public enum EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    CANCELADA
}
```

### Entidad: MensajePrivado

```java
@Entity
@Table(name = "mensajes_privados", indexes = {
    @Index(name = "idx_conversacion", columnList = "remitente_id,destinatario_id"),
    @Index(name = "idx_fecha", columnList = "fecha_envio")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajePrivado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;
    
    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMensaje tipo = TipoMensaje.TEXTO;
    
    @Column(columnDefinition = "TEXT")
    private String contenido;  // Texto, URL de audio, URL de GIF
    
    @Column(nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(nullable = false)
    private Boolean leido = false;
    
    private LocalDateTime fechaLectura;
    
    @Column(nullable = false)
    private Boolean eliminadoPorRemitente = false;
    
    @Column(nullable = false)
    private Boolean eliminadoPorDestinatario = false;
    
    // Para mensajes de audio
    private Integer duracionAudio;  // en segundos
    
    // Para respuestas a mensajes
    @ManyToOne
    @JoinColumn(name = "mensaje_respondido_id")
    private MensajePrivado mensajeRespondido;
}

public enum TipoMensaje {
    TEXTO,
    AUDIO,
    GIF,
    STICKER,
    IMAGEN,
    INVITACION_PARTIDA,
    TRANSFERENCIA_FICHAS
}
```

### Entidad: TransferenciaFichas

```java
@Entity
@Table(name = "transferencias_fichas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaFichas {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;
    
    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;
    
    @Column(nullable = false)
    private Long cantidad;
    
    @Column(nullable = false)
    private LocalDateTime fecha;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransferencia estado = EstadoTransferencia.COMPLETADA;
    
    private String mensaje;  // Mensaje opcional
    
    @Column(nullable = false)
    private Boolean esRegalo = false;  // Si es regalo o préstamo
}

public enum EstadoTransferencia {
    PENDIENTE,
    COMPLETADA,
    RECHAZADA,
    CANCELADA,
    REVERTIDA  // Por moderación
}
```

### Entidad: InvitacionPartida

```java
@Entity
@Table(name = "invitaciones_partida")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitacionPartida {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;
    
    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;
    
    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInvitacion tipo = TipoInvitacion.JUGADOR;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaEnvio;
    
    private LocalDateTime fechaExpiracion;
    
    private LocalDateTime fechaRespuesta;
    
    private String mensaje;
}

public enum TipoInvitacion {
    JUGADOR,      // Unirse como jugador
    ESPECTADOR    // Unirse como espectador
}

public enum EstadoInvitacion {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    EXPIRADA,
    CANCELADA
}
```

### Entidad: ConfiguracionPrivacidad

```java
@Entity
@Table(name = "configuracion_privacidad")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionPrivacidad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelPrivacidad quienPuedeEnviarSolicitudes = NivelPrivacidad.TODOS;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelPrivacidad quienPuedeVerEstado = NivelPrivacidad.AMIGOS;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelPrivacidad quienPuedeInvitar = NivelPrivacidad.AMIGOS;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelPrivacidad quienPuedeTransferirFichas = NivelPrivacidad.AMIGOS;
    
    @Column(nullable = false)
    private Boolean mostrarEstadisticas = true;
    
    @Column(nullable = false)
    private Boolean aceptarSolicitudesAutomaticamente = false;
    
    @Column(nullable = false)
    private Boolean notificarConexion = true;
    
    @Column(nullable = false)
    private Boolean notificarInicioPartida = true;
    
    @Column(nullable = false)
    private Boolean modoPerturbacion = false;  // No molestar
}

public enum NivelPrivacidad {
    TODOS,
    AMIGOS,
    AMIGOS_DE_AMIGOS,
    NADIE
}
```

---

<a id="funcionalidades-principales"></a>
## 🎮 Funcionalidades principales

### 1. Gestión de amistades

#### Enviar solicitud de amistad
- Buscar usuarios por username o email
- Añadir mensaje personalizado opcional
- Límite de solicitudes pendientes (máx. 50)
- Prevención de spam (máx. 5 solicitudes por día al mismo usuario)

#### Aceptar/rechazar solicitudes
- Notificación en tiempo real al remitente
- Opción de bloquear al usuario al rechazar
- Las solicitudes pendientes expiran en 30 días

#### Eliminar amigo
- Confirmación requerida
- Elimina todo el historial de chat (opcional)
- Cancela invitaciones pendientes

### 2. Lista de amigos

#### Visualización
- **Estados posibles:**
    - 🟢 En línea (idle/activo)
    - 🎮 En partida (nombre de mesa)
    - 🏆 En torneo (nombre de torneo)
    - 🔴 Desconectado (última conexión)
    - ⏸️ Ausente (AFK)
    - 🚫 No molestar

#### Organización
- Filtros: En línea, Favoritos, Todos
- Ordenar por: Estado, Alfabético, Última conexión, Nivel
- Búsqueda rápida por nombre/alias
- Secciones colapsables (Favoritos, En línea, Desconectados)

#### Información de amigo
- Avatar y nivel
- Fichas actuales
- Racha de victorias
- Logros destacados (últimos 3)
- Estadísticas públicas
- Alias personalizado

### 3. Estado de presencia en tiempo real

```java
public class EstadoPresencia {
    private Long userId;
    private EstadoConexion estado;
    private String detalleEstado;  // "Jugando en Mesa VIP"
    private LocalDateTime ultimaActividad;
    private Long mesaId;
    private Long torneoId;
    private Boolean aceptaInvitaciones;
}

public enum EstadoConexion {
    ONLINE,
    EN_PARTIDA,
    EN_TORNEO,
    AUSENTE,
    NO_MOLESTAR,
    OFFLINE
}
```

---

<a id="endpoints-rest"></a>
## 🌐 Endpoints REST

### Gestión de amistades

```http
# Buscar usuarios
GET /api/amigos/buscar?q=username&limit=20
Authorization: Bearer {token}

# Enviar solicitud
POST /api/amigos/solicitudes/enviar
Authorization: Bearer {token}
Content-Type: application/json

{
  "destinatarioId": 5,
  "mensaje": "¡Hola! Jugamos juntos ayer, fue genial."
}

# Listar solicitudes recibidas
GET /api/amigos/solicitudes/recibidas?page=0&size=20
Authorization: Bearer {token}

# Listar solicitudes enviadas
GET /api/amigos/solicitudes/enviadas
Authorization: Bearer {token}

# Aceptar solicitud
POST /api/amigos/solicitudes/{solicitudId}/aceptar
Authorization: Bearer {token}

# Rechazar solicitud
POST /api/amigos/solicitudes/{solicitudId}/rechazar
Authorization: Bearer {token}
Content-Type: application/json

{
  "bloquear": false
}

# Cancelar solicitud enviada
DELETE /api/amigos/solicitudes/{solicitudId}
Authorization: Bearer {token}

# Listar amigos
GET /api/amigos?filtro=online&ordenar=estado
Authorization: Bearer {token}

# Ver detalles de un amigo
GET /api/amigos/{userId}
Authorization: Bearer {token}

# Eliminar amigo
DELETE /api/amigos/{userId}
Authorization: Bearer {token}

# Marcar/desmarcar como favorito
PUT /api/amigos/{userId}/favorito
Authorization: Bearer {token}
Content-Type: application/json

{
  "esFavorito": true
}

# Establecer alias personalizado
PUT /api/amigos/{userId}/alias
Authorization: Bearer {token}
Content-Type: application/json

{
  "alias": "El Maestro"
}

# Configurar notificaciones de un amigo
PUT /api/amigos/{userId}/notificaciones
Authorization: Bearer {token}
Content-Type: application/json

{
  "activas": false
}
```

---

### Chat privado

```http
# Obtener conversación
GET /api/chat/conversacion/{amigoId}?page=0&size=50
Authorization: Bearer {token}

# Enviar mensaje de texto
POST /api/chat/mensaje
Authorization: Bearer {token}
Content-Type: application/json

{
  "destinatarioId": 5,
  "tipo": "TEXTO",
  "contenido": "¡Buena partida!",
  "mensajeRespondidoId": null
}

# Enviar audio (URL después de upload)
POST /api/chat/mensaje
Authorization: Bearer {token}
Content-Type: application/json

{
  "destinatarioId": 5,
  "tipo": "AUDIO",
  "contenido": "https://cdn.poker.com/audios/user1/msg123.webm",
  "duracionAudio": 15
}

# Enviar GIF/Sticker
POST /api/chat/mensaje
Authorization: Bearer {token}
Content-Type: application/json

{
  "destinatarioId": 5,
  "tipo": "GIF",
  "contenido": "https://media.giphy.com/media/xyz/giphy.gif"
}

# Marcar mensajes como leídos
PUT /api/chat/conversacion/{amigoId}/leer
Authorization: Bearer {token}

# Eliminar mensaje
DELETE /api/chat/mensaje/{mensajeId}
Authorization: Bearer {token}

# Buscar en conversación
GET /api/chat/conversacion/{amigoId}/buscar?q=fichas&limit=20
Authorization: Bearer {token}

# Obtener mensajes no leídos (total)
GET /api/chat/no-leidos
Authorization: Bearer {token}

# Upload de audio
POST /api/chat/upload/audio
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [archivo .webm, .ogg o .mp3]

Response:
{
  "url": "https://cdn.poker.com/audios/user1/msg123.webm",
  "duracion": 15
}
```

---

## Transferencia de fichas

```http
# Enviar fichas
POST /api/amigos/transferir-fichas
Authorization: Bearer {token}
Content-Type: application/json

{
  "destinatarioId": 5,
  "cantidad": 5000,
  "mensaje": "¡Para tu próximo torneo!",
  "esRegalo": true
}

# Historial de transferencias
GET /api/amigos/transferencias?page=0&size=20
Authorization: Bearer {token}

# Ver transferencia específica
GET /api/amigos/transferencias/{transferenciaId}
Authorization: Bearer {token}

# Límites de transferencia
GET /api/amigos/transferencias/limites
Authorization: Bearer {token}

Response:
{
  "limiteDiario": 50000,
  "usadoHoy": 15000,
  "restanteHoy": 35000,
  "limitePorTransferencia": 10000
}
```

---

## Invitaciones a partidas

```http
# Invitar a partida
POST /api/amigos/invitaciones/enviar
Authorization: Bearer {token}
Content-Type: application/json

{
  "destinatarioId": 5,
  "mesaId": 3,
  "tipo": "JUGADOR",
  "mensaje": "¡Ven a jugar con nosotros!"
}

# Listar invitaciones recibidas
GET /api/amigos/invitaciones/recibidas
Authorization: Bearer {token}

# Aceptar invitación (une automáticamente)
POST /api/amigos/invitaciones/{invitacionId}/aceptar
Authorization: Bearer {token}

# Rechazar invitación
POST /api/amigos/invitaciones/{invitacionId}/rechazar
Authorization: Bearer {token}

# Cancelar invitación enviada
DELETE /api/amigos/invitaciones/{invitacionId}
Authorization: Bearer {token}

# Invitación rápida (quick join)
POST /api/amigos/{userId}/invitar-rapido
Authorization: Bearer {token}

Response:
{
  "mesaId": 3,
  "codigoAcceso": "ABC123",  // Si es mesa privada
  "mensaje": "Tu amigo te ha invitado a su mesa"
}
```

---

### Estado y presencia

```http
# Ver estado de amigos
GET /api/amigos/estados
Authorization: Bearer {token}

Response:
[
  {
    "userId": 5,
    "username": "Alice",
    "estado": "EN_PARTIDA",
    "detalleEstado": "Jugando en Mesa VIP #3",
    "mesaId": 3,
    "puedeUnirse": true,
    "puedeEspectador": true
  },
  ...
]

# Cambiar mi estado
PUT /api/amigos/mi-estado
Authorization: Bearer {token}
Content-Type: application/json

{
  "estado": "NO_MOLESTAR"
}

# Ver quién está viendo mi perfil (últimos 10)
GET /api/amigos/visitas-perfil
Authorization: Bearer {token}
```

---

### Configuración de privacidad

```http
# Obtener configuración
GET /api/amigos/privacidad
Authorization: Bearer {token}

# Actualizar configuración
PUT /api/amigos/privacidad
Authorization: Bearer {token}
Content-Type: application/json

{
  "quienPuedeEnviarSolicitudes": "TODOS",
  "quienPuedeVerEstado": "AMIGOS",
  "quienPuedeInvitar": "AMIGOS",
  "quienPuedeTransferirFichas": "AMIGOS",
  "mostrarEstadisticas": true,
  "notificarConexion": true,
  "notificarInicioPartida": false,
  "modoPerturbacion": false
}

# Bloquear usuario
POST /api/amigos/bloquear/{userId}
Authorization: Bearer {token}

# Desbloquear usuario
DELETE /api/amigos/bloquear/{userId}
Authorization: Bearer {token}

# Listar bloqueados
GET /api/amigos/bloqueados
Authorization: Bearer {token}
```

---

<a id="websocket-eventos-en-tiempo-real"></a>
## 🔌 WebSocket: eventos en tiempo real

### Suscripciones

```javascript
// Estado de amigos
stompClient.subscribe('/user/queue/amigos/estados', (message) => {
  const estados = JSON.parse(message.body);
  actualizarListaAmigos(estados);
});

// Nuevos mensajes de chat
stompClient.subscribe('/user/queue/chat/mensajes', (message) => {
  const mensaje = JSON.parse(message.body);
  mostrarNotificacionMensaje(mensaje);
});

// Solicitudes de amistad
stompClient.subscribe('/user/queue/amigos/solicitudes', (message) => {
  const solicitud = JSON.parse(message.body);
  mostrarNotificacionSolicitud(solicitud);
});

// Invitaciones a partidas
stompClient.subscribe('/user/queue/amigos/invitaciones', (message) => {
  const invitacion = JSON.parse(message.body);
  mostrarNotificacionInvitacion(invitacion);
});

// Transferencias de fichas
stompClient.subscribe('/user/queue/amigos/transferencias', (message) => {
  const transferencia = JSON.parse(message.body);
  mostrarNotificacionTransferencia(transferencia);
});
```

### Eventos enviados

| Evento | Descripción | Payload |
|--------|-------------|---------|
| `amigo_conectado` | Amigo se conecta | `{ userId, username, timestamp }` |
| `amigo_desconectado` | Amigo se desconecta | `{ userId, username, timestamp }` |
| `amigo_cambio_estado` | Amigo cambia estado | `{ userId, nuevoEstado, detalleEstado }` |
| `solicitud_amistad` | Nueva solicitud recibida | `SolicitudAmistadDTO` |
| `solicitud_aceptada` | Solicitud fue aceptada | `{ userId, username }` |
| `solicitud_rechazada` | Solicitud fue rechazada | `{ userId }` |
| `mensaje_chat` | Nuevo mensaje privado | `MensajePrivadoDTO` |
| `mensaje_leido` | Mensajes marcados como leídos | `{ remitenteId, cantidad }` |
| `invitacion_partida` | Invitación recibida | `InvitacionPartidaDTO` |
| `invitacion_aceptada` | Invitación aceptada | `{ userId, mesaId }` |
| `transferencia_recibida` | Fichas recibidas | `TransferenciaFichasDTO` |
| `amigo_eliminado` | Fuiste eliminado de amigos | `{ userId, username }` |

---

<a id="sistema-de-chat-privado"></a>
## 💬 Sistema de chat privado

### Características

#### Mensajes de texto
- Máximo 1000 caracteres
- Soporte de emojis Unicode
- Menciones con @username
- Links automáticamente detectados

#### Mensajes de audio
- Formato: WebM, OGG o MP3
- Duración máxima: 2 minutos
- Tamaño máximo: 5MB
- Compresión automática
- Reproducción en línea

#### GIFs y Stickers
- Integración con Giphy/Tenor
- Categorías predefinidas
- Búsqueda en biblioteca
- Stickers personalizados del juego (logros, cartas)

#### Funciones avanzadas
- Responder a mensaje específico (quote)
- Editar mensajes (15 minutos después de enviar)
- Eliminar mensajes (solo para ti o para ambos)
- Reacciones rápidas (👍 ❤️ 😂 😮 😢 👎)
- Mensajes temporales (se autodestruyen)
- Indicador de "escribiendo..."
- Confirmación de lectura (doble check azul)

### Almacenamiento

- Historial: últimos 10,000 mensajes por conversación
- Mensajes más antiguos se archivan
- Opción de descargar historial completo
- Búsqueda de texto completo con índices

### Moderación

- Filtro de palabras ofensivas
- Reportar mensaje inapropiado
- Bloqueo automático tras múltiples reportes
- Los admins pueden ver reportes y tomar acción

---

<a id="transferencia-de-fichas"></a>
## 💰 Transferencia de fichas

### Límites y restricciones

| Concepto | Límite |
|----------|--------|
| Transferencia mínima | 100 fichas |
| Transferencia máxima | 10,000 fichas |
| Límite diario por usuario | 50,000 fichas |
| Máximo de transferencias diarias | 10 |
| Cooldown entre transferencias | 5 minutos |
| Fichas mínimas para transferir | 1,000 fichas |

### Validaciones

1. **Ambos usuarios deben ser amigos** hace mínimo 7 días
2. **Remitente** debe tener suficientes fichas + 10% de comisión
3. **Destinatario** no puede estar sancionado
4. **No se permite** transferir a bots
5. **Registro obligatorio** para auditoría
6. **Reversible** por admins en caso de fraude

### Proceso

```
1. Usuario solicita transferencia
   ↓
2. Sistema valida límites y estado de ambos usuarios
   ↓
3. Se aplica comisión del 10% (va al sistema)
   ↓
4. Se descuentan fichas del remitente
   ↓
5. Se acreditan fichas al destinatario
   ↓
6. Se registra en TransferenciaFichas
   ↓
7. Notificación push + WebSocket a ambos usuarios
   ↓
8. Registro en log de auditoría
```

### Comisiones

- **Regalo:** 10% de comisión (evita abuso)
- **Devolución:** 5% si es dentro de 24h de una transferencia anterior
- **Eventos especiales:** 0% comisión (definido por admins)

### Historial

```json
{
  "id": 123,
  "remitente": {
    "userId": 1,
    "username": "Alice"
  },
  "destinatario": {
    "userId": 5,
    "username": "Bob"
  },
  "cantidad": 5000,
  "comision": 500,
  "cantidadNeta": 4500,
  "mensaje": "¡Para tu torneo!",
  "esRegalo": true,
  "estado": "COMPLETADA",
  "fecha": "2025-09-30T14:30:00Z"
}
```

---

<a id="invitaciones-a-partidas"></a>
## 🎮 Invitaciones a partidas

### Tipos de invitación

#### 1. Unirse como jugador
- Solo si hay espacio disponible
- Requiere fichas mínimas
- Si es mesa privada, genera código de acceso temporal
- Expira en 5 minutos

#### 2. Unirse como espectador
- Sin límite de espectadores
- No requiere fichas
- Puede ver la partida en tiempo real
- Acceso al chat de espectadores

### Flujo de invitación

```
1. Usuario A está en una partida
   ↓
2. Abre lista de amigos en línea
   ↓
3. Selecciona amigo(s) y tipo (jugador/espectador)
   ↓
4. Sistema verifica disponibilidad de mesa
   ↓
5. Envía invitación via WebSocket + notificación
   ↓
6. Usuario B recibe notificación con botón "Unirse"
   ↓
7. Al aceptar, se une automáticamente a la mesa
   ↓
8. Confirmación a Usuario A
```

### Invitación rápida

Función "quick join" que permite a un amigo unirse con un solo clic:

```http
POST /api/amigos/{userId}/quick-join
```

Genera un código temporal de 5 minutos que permite acceso directo.

### Notificaciones web

Las invitaciones aparecen como:
- Notificación push (móvil)
- Banner en la interfaz (web)
- Mensaje en el chat privado
- Email (si configurado)

---

<a id="sistema-de-presencia"></a>
## 🟢 Sistema de presencia

### Estados disponibles

```java
public enum EstadoConexion {
    ONLINE,          // Conectado, navegando
    EN_PARTIDA,      // Jugando activamente
    EN_TORNEO,       // Participando en torneo
    AUSENTE,         // AFK (sin actividad 10+ min)
    NO_MOLESTAR,     // Configurado manualmente
    OFFLINE          // Desconectado
}
```

### Detalles contextuales

Cada estado puede tener información adicional:

| Estado | Detalle ejemplo |
|--------|-----------------|
| ONLINE | "Navegando en lobby" |
| EN_PARTIDA | "Mesa VIP #3 - Texas Hold'em" |
| EN_TORNEO | "Torneo Mensual - Ronda 2/5" |
| AUSENTE | "Ausente desde hace 15 min" |
| NO_MOLESTAR | "No molestar" |
| OFFLINE | "Última vez: hace 2 horas" |

### Actualización automática

El sistema actualiza estados mediante:

1. **WebSocket heartbeat** cada 30 segundos
2. **Eventos del juego:**
    - Unirse a mesa → EN_PARTIDA
    - Salir de mesa → ONLINE
    - Inactividad 10 min → AUSENTE
    - Cerrar sesión → OFFLINE
3. **Cambio manual** por el usuario

### Privacidad

Los usuarios pueden configurar:
- Quién puede ver su estado (Todos/Amigos/Nadie)
- Ocultar detalles específicos (nombre de mesa/torneo)
- Aparecer siempre como "Offline"
- Modo invisible (ve estados, pero aparece offline)

---

<a id="notificaciones"></a>
## 🔔 Notificaciones

### Tipos de notificación

#### En la aplicación
```json
{
  "id": "notif_123",
  "tipo": "SOLICITUD_AMISTAD",
  "titulo": "Nueva solicitud de amistad",
  "mensaje": "Alice quiere ser tu amigo",
  "avatarUrl": "https://...",
  "timestamp": "2025-09-30T14:30:00Z",
  "accion": {
    "tipo": "ABRIR_SOLICITUDES",
    "url": "/amigos/solicitudes"
  },
  "leida": false
}
```

#### Push (móvil/web)
```json
{
  "title": "🎮 Alice te invitó a jugar",
  "body": "Únete a Mesa VIP #3",
  "icon": "https://cdn.poker.com/icons/invitacion.png",
  "badge": "https://cdn.poker.com/badges/poker.png",
  "data": {
    "tipo": "INVITACION_PARTIDA",
    "invitacionId": 456,
    "mesaId": 3
  },
  "actions": [
    {
      "action": "aceptar",
      "title": "Unirse"
    },
    {
      "action": "rechazar",
      "title": "Rechazar"
    }
  ]
}
```

### Configuración granular

Los usuarios pueden activar/desactivar:

- ✅ Solicitudes de amistad
- ✅ Mensajes de chat
- ✅ Invitaciones a partidas
- ✅ Transferencias de fichas
- ✅ Amigo se conecta
- ✅ Amigo inicia partida
- ✅ Amigo obtiene logro
- ✅ Recordatorio de mensajes sin leer

### Agrupación inteligente

Si hay múltiples notificaciones del mismo tipo:
```
"Alice, Bob y 3 amigos más están en línea"
"5 nuevos mensajes de 3 conversaciones"
```

### Badges y contadores

- Círculo rojo con número en ícono de chat
- Contador en lista de amigos (solicitudes pendientes)
- Badge en perfil (notificaciones sin leer)

---

<a id="privacidad-y-configuracion"></a>
## 🔒 Privacidad y configuración

### Niveles de privacidad

#### TODOS
Cualquier usuario registrado puede realizar la acción.

#### AMIGOS
Solo usuarios en tu lista de amigos.

#### AMIGOS_DE_AMIGOS
Usuarios que tienen amigos en común contigo.

#### NADIE
Nadie puede realizar la acción (completamente privado).

### Opciones configurables

```java
public class ConfiguracionPrivacidad {
    // Quién puede enviar solicitudes
    private NivelPrivacidad quienPuedeEnviarSolicitudes;
    
    // Quién puede ver tu estado (online/offline/en partida)
    private NivelPrivacidad quienPuedeVerEstado;
    
    // Quién puede invitarte a partidas
    private NivelPrivacidad quienPuedeInvitar;
    
    // Quién puede transferirte fichas
    private NivelPrivacidad quienPuedeTransferirFichas;
    
    // Mostrar estadísticas en perfil público
    private Boolean mostrarEstadisticas;
    
    // Aceptar solicitudes automáticamente (de amigos de amigos)
    private Boolean aceptarSolicitudesAutomaticamente;
    
    // Notificar cuando te conectas
    private Boolean notificarConexion;
    
    // Notificar cuando inicias partida
    private Boolean notificarInicioPartida;
    
    // Modo no molestar (rechaza invitaciones automáticamente)
    private Boolean modoPerturbacion;
}
```

### Bloqueo de usuarios

Al bloquear un usuario:
1. Se elimina la amistad si existía
2. No puede enviarte solicitudes
3. No puede verte en búsquedas
4. No puede ver tu perfil
5. No recibe notificaciones tuyas
6. No puede unirse a tus mesas
7. Los mensajes antiguos se ocultan (no se borran)

---

<a id="limites-y-restricciones"></a>
## ⚠️ Límites y restricciones

### Solicitudes de amistad

| Límite | Valor |
|--------|-------|
| Máximo pendientes enviadas | 50 |
| Máximo por día | 20 |
| Máximo al mismo usuario/día | 5 |
| Expiración | 30 días |
| Cooldown tras rechazo | 7 días |

### Lista de amigos

| Límite | Valor |
|--------|-------|
| Máximo de amigos | 500 (usuarios normales) |
| Máximo de amigos | 1000 (usuarios VIP) |
| Máximo favoritos | 50 |
| Máximo usuarios bloqueados | 200 |

### Chat privado

| Límite | Valor |
|--------|-------|
| Mensajes de texto | 1000 caracteres |
| Audios | 2 minutos / 5MB |
| Imágenes | 10MB |
| GIFs | URL externa |
| Mensajes por minuto | 20 |
| Mensajes sin leer máx. | 1000 |

### Transferencias

| Límite | Valor |
|--------|-------|
| Mínimo por transferencia | 100 fichas |
| Máximo por transferencia | 10,000 fichas |
| Límite diario | 50,000 fichas |
| Cantidad de transferencias/día | 10 |
| Cooldown entre transfers | 5 minutos |
| Días mínimos de amistad | 7 días |

### Invitaciones

| Límite | Valor |
|--------|-------|
| Invitaciones activas máx. | 10 |
| Duración invitación | 5 minutos |
| Cooldown por usuario | 2 minutos |
| Rechazos antes de bloqueo temp. | 5 consecutivos |

---

<a id="implementacion-tecnica"></a>
## 💻 Implementación técnica

### Service: AmigosService

```java
@Service
@RequiredArgsConstructor
public class AmigosService {
    
    private final AmistadRepository amistadRepository;
    private final SolicitudAmistadRepository solicitudRepository;
    private final UserRepository userRepository;
    private final ConfiguracionPrivacidadRepository configRepository;
    private final NotificacionService notificacionService;
    private final WebSocketService webSocketService;
    
    /**
     * Envía una solicitud de amistad
     */
    public SolicitudAmistadDTO enviarSolicitud(Long remitenteId, Long destinatarioId, String mensaje) {
        // Validaciones
        validarNoEsElMismo(remitenteId, destinatarioId);
        validarNoSonAmigos(remitenteId, destinatarioId);
        validarNoExisteSolicitudPendiente(remitenteId, destinatarioId);
        validarLimiteDiario(remitenteId);
        validarPrivacidadDestinatario(remitenteId, destinatarioId);
        
        User remitente = userRepository.findById(remitenteId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        User destinatario = userRepository.findById(destinatarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Crear solicitud
        SolicitudAmistad solicitud = SolicitudAmistad.builder()
            .remitente(remitente)
            .destinatario(destinatario)
            .mensaje(mensaje)
            .estado(EstadoSolicitud.PENDIENTE)
            .fechaEnvio(LocalDateTime.now())
            .build();
        
        solicitud = solicitudRepository.save(solicitud);
        
        // Notificar al destinatario
        notificacionService.enviarNotificacion(
            destinatarioId,
            TipoNotificacion.SOLICITUD_AMISTAD,
            "Nueva solicitud de amistad",
            remitente.getUsername() + " quiere ser tu amigo",
            Map.of("solicitudId", solicitud.getId())
        );
        
        // WebSocket
        webSocketService.enviarAUsuario(
            destinatarioId,
            "/queue/amigos/solicitudes",
            SolicitudAmistadDTO.fromEntity(solicitud)
        );
        
        return SolicitudAmistadDTO.fromEntity(solicitud);
    }
    
    /**
     * Acepta una solicitud de amistad
     */
    @Transactional
    public AmistadDTO aceptarSolicitud(Long solicitudId, Long userId) {
        SolicitudAmistad solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        // Validar que el usuario es el destinatario
        if (!solicitud.getDestinatario().getId().equals(userId)) {
            throw new UnauthorizedException("No puedes aceptar esta solicitud");
        }
        
        // Validar que está pendiente
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud ya fue respondida");
        }
        
        // Actualizar solicitud
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        
        // Crear amistad
        Amistad amistad = Amistad.builder()
            .usuario1(solicitud.getRemitente())
            .usuario2(solicitud.getDestinatario())
            .fechaAmistad(LocalDateTime.now())
            .esFavorito1(false)
            .esFavorito2(false)
            .notificacionesActivas1(true)
            .notificacionesActivas2(true)
            .build();
        
        amistad = amistadRepository.save(amistad);
        
        // Notificar al remitente
        notificacionService.enviarNotificacion(
            solicitud.getRemitente().getId(),
            TipoNotificacion.SOLICITUD_ACEPTADA,
            "Solicitud aceptada",
            solicitud.getDestinatario().getUsername() + " aceptó tu solicitud",
            Map.of("userId", solicitud.getDestinatario().getId())
        );
        
        // WebSocket a ambos
        webSocketService.enviarAUsuario(
            solicitud.getRemitente().getId(),
            "/queue/amigos/solicitudes",
            Map.of(
                "tipo", "SOLICITUD_ACEPTADA",
                "userId", solicitud.getDestinatario().getId(),
                "username", solicitud.getDestinatario().getUsername()
            )
        );
        
        return AmistadDTO.fromEntity(amistad, userId);
    }
    
    /**
     * Obtiene lista de amigos con su estado actual
     */
    public List<AmigoDTO> obtenerAmigos(Long userId, FiltroAmigos filtro, OrdenAmigos orden) {
        List<Amistad> amistades = amistadRepository.findByUsuario(userId);
        
        return amistades.stream()
            .map(amistad -> {
                User amigo = amistad.getUsuario1().getId().equals(userId) 
                    ? amistad.getUsuario2() 
                    : amistad.getUsuario1();
                
                // Obtener estado de presencia
                EstadoPresencia estado = presenciaService.obtenerEstado(amigo.getId());
                
                return AmigoDTO.builder()
                    .userId(amigo.getId())
                    .username(amigo.getUsername())
                    .avatarUrl(amigo.getAvatarUrl())
                    .nivel(amigo.getNivel())
                    .estado(estado.getEstado())
                    .detalleEstado(estado.getDetalleEstado())
                    .ultimaConexion(estado.getUltimaActividad())
                    .esFavorito(esUsuario1(amistad, userId) ? amistad.getEsFavorito1() : amistad.getEsFavorito2())
                    .alias(esUsuario1(amistad, userId) ? amistad.getAlias1() : amistad.getAlias2())
                    .puedeUnirse(estado.isAceptaInvitaciones() && estado.getMesaId() != null)
                    .mesaId(estado.getMesaId())
                    .torneoId(estado.getTorneoId())
                    .fichas(amigo.getFichas())
                    .build();
            })
            .filter(amigo -> aplicarFiltro(amigo, filtro))
            .sorted(obtenerComparador(orden))
            .collect(Collectors.toList());
    }
    
    /**
     * Elimina un amigo
     */
    @Transactional
    public void eliminarAmigo(Long userId, Long amigoId, boolean eliminarHistorialChat) {
        Amistad amistad = amistadRepository.findByUsuarios(userId, amigoId)
            .orElseThrow(() -> new ResourceNotFoundException("Amistad no encontrada"));
        
        // Eliminar amistad
        amistadRepository.delete(amistad);
        
        // Cancelar invitaciones pendientes
        invitacionService.cancelarInvitacionesEntre(userId, amigoId);
        
        // Eliminar historial de chat si se solicita
        if (eliminarHistorialChat) {
            chatPrivadoService.eliminarConversacion(userId, amigoId);
        }
        
        // Notificar al otro usuario
        notificacionService.enviarNotificacion(
            amigoId,
            TipoNotificacion.AMIGO_ELIMINADO,
            "Amistad eliminada",
            "Ya no eres amigo de " + obtenerUsername(userId),
            Map.of("userId", userId)
        );
        
        // WebSocket
        webSocketService.enviarAUsuario(
            amigoId,
            "/queue/amigos/estados",
            Map.of(
                "tipo", "AMIGO_ELIMINADO",
                "userId", userId
            )
        );
    }
    
    // Métodos auxiliares de validación
    
    private void validarNoEsElMismo(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new BadRequestException("No puedes enviarte solicitud a ti mismo");
        }
    }
    
    private void validarNoSonAmigos(Long userId1, Long userId2) {
        if (amistadRepository.existeAmistad(userId1, userId2)) {
            throw new BadRequestException("Ya son amigos");
        }
    }
    
    private void validarNoExisteSolicitudPendiente(Long remitenteId, Long destinatarioId) {
        if (solicitudRepository.existePendiente(remitenteId, destinatarioId)) {
            throw new BadRequestException("Ya existe una solicitud pendiente");
        }
    }
    
    private void validarLimiteDiario(Long userId) {
        long solicitudesHoy = solicitudRepository.contarSolicitudesHoy(userId);
        if (solicitudesHoy >= 20) {
            throw new BadRequestException("Límite diario de solicitudes alcanzado");
        }
    }
    
    private void validarPrivacidadDestinatario(Long remitenteId, Long destinatarioId) {
        ConfiguracionPrivacidad config = configRepository.findByUserId(destinatarioId)
            .orElse(ConfiguracionPrivacidad.builder()
                .quienPuedeEnviarSolicitudes(NivelPrivacidad.TODOS)
                .build());
        
        switch (config.getQuienPuedeEnviarSolicitudes()) {
            case NADIE:
                throw new ForbiddenException("Este usuario no acepta solicitudes");
            case AMIGOS:
                if (!amistadRepository.existeAmistad(remitenteId, destinatarioId)) {
                    throw new ForbiddenException("Solo acepta solicitudes de amigos");
                }
                break;
            case AMIGOS_DE_AMIGOS:
                if (!amistadRepository.tienenAmigosEnComun(remitenteId, destinatarioId)) {
                    throw new ForbiddenException("Solo acepta solicitudes de amigos de amigos");
                }
                break;
            case TODOS:
                // Permitir
                break;
        }
    }
}
```

### Service: ChatPrivadoService

```java
@Service
@RequiredArgsConstructor
public class ChatPrivadoService {
    
    private final MensajePrivadoRepository mensajeRepository;
    private final AmistadRepository amistadRepository;
    private final WebSocketService webSocketService;
    private final NotificacionService notificacionService;
    private final ModerationService moderationService;
    
    /**
     * Envía un mensaje privado
     */
    @Transactional
    public MensajePrivadoDTO enviarMensaje(Long remitenteId, CrearMensajeDTO dto) {
        // Validar que son amigos
        if (!amistadRepository.existeAmistad(remitenteId, dto.getDestinatarioId())) {
            throw new ForbiddenException("Solo puedes enviar mensajes a tus amigos");
        }
        
        // Validar límite de mensajes por minuto
        validarLimiteVelocidad(remitenteId);
        
        // Validar contenido
        if (dto.getTipo() == TipoMensaje.TEXTO) {
            dto.setContenido(moderationService.filtrarContenido(dto.getContenido()));
        }
        
        User remitente = userRepository.findById(remitenteId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        User destinatario = userRepository.findById(dto.getDestinatarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Crear mensaje
        MensajePrivado mensaje = MensajePrivado.builder()
            .remitente(remitente)
            .destinatario(destinatario)
            .tipo(dto.getTipo())
            .contenido(dto.getContenido())
            .fechaEnvio(LocalDateTime.now())
            .leido(false)
            .duracionAudio(dto.getDuracionAudio())
            .build();
        
        // Si es respuesta a otro mensaje
        if (dto.getMensajeRespondidoId() != null) {
            MensajePrivado mensajeRespondido = mensajeRepository.findById(dto.getMensajeRespondidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));
            mensaje.setMensajeRespondido(mensajeRespondido);
        }
        
        mensaje = mensajeRepository.save(mensaje);
        
        // Enviar por WebSocket
        MensajePrivadoDTO mensajeDTO = MensajePrivadoDTO.fromEntity(mensaje);
        webSocketService.enviarAUsuario(
            dto.getDestinatarioId(),
            "/queue/chat/mensajes",
            mensajeDTO
        );
        
        // Notificación push si el destinatario no está conectado
        if (!presenciaService.estaConectado(dto.getDestinatarioId())) {
            notificacionService.enviarPushNotification(
                dto.getDestinatarioId(),
                "Nuevo mensaje de " + remitente.getUsername(),
                obtenerVistaPrevia(mensaje),
                Map.of("tipo", "MENSAJE_CHAT", "remitenteId", remitenteId)
            );
        }
        
        return mensajeDTO;
    }
    
    /**
     * Obtiene conversación con paginación
     */
    public Page<MensajePrivadoDTO> obtenerConversacion(
        Long userId, 
        Long amigoId, 
        Pageable pageable
    ) {
        // Validar que son amigos
        if (!amistadRepository.existeAmistad(userId, amigoId)) {
            throw new ForbiddenException("No tienes permiso para ver esta conversación");
        }
        
        Page<MensajePrivado> mensajes = mensajeRepository.findConversacion(
            userId, 
            amigoId, 
            pageable
        );
        
        return mensajes.map(MensajePrivadoDTO::fromEntity);
    }
    
    /**
     * Marca mensajes como leídos
     */
    @Transactional
    public void marcarComoLeidos(Long userId, Long remitenteId) {
        List<MensajePrivado> mensajesNoLeidos = mensajeRepository
            .findNoLeidosDeRemitente(userId, remitenteId);
        
        if (!mensajesNoLeidos.isEmpty()) {
            mensajesNoLeidos.forEach(m -> {
                m.setLeido(true);
                m.setFechaLectura(LocalDateTime.now());
            });
            mensajeRepository.saveAll(mensajesNoLeidos);
            
            // Notificar al remitente
            webSocketService.enviarAUsuario(
                remitenteId,
                "/queue/chat/mensajes",
                Map.of(
                    "tipo", "MENSAJES_LEIDOS",
                    "destinatarioId", userId,
                    "cantidad", mensajesNoLeidos.size()
                )
            );
        }
    }
    
    /**
     * Obtiene cantidad total de mensajes no leídos
     */
    public int contarNoLeidos(Long userId) {
        return mensajeRepository.countNoLeidos(userId);
    }
    
    /**
     * Elimina un mensaje
     */
    @Transactional
    public void eliminarMensaje(Long mensajeId, Long userId, boolean paraAmbos) {
        MensajePrivado mensaje = mensajeRepository.findById(mensajeId)
            .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));
        
        // Validar que el usuario es parte de la conversación
        boolean esRemitente = mensaje.getRemitente().getId().equals(userId);
        boolean esDestinatario = mensaje.getDestinatario().getId().equals(userId);
        
        if (!esRemitente && !esDestinatario) {
            throw new ForbiddenException("No tienes permiso para eliminar este mensaje");
        }
        
        if (paraAmbos && !esRemitente) {
            throw new ForbiddenException("Solo el remitente puede eliminar para ambos");
        }
        
        if (paraAmbos) {
            mensaje.setEliminadoPorRemitente(true);
            mensaje.setEliminadoPorDestinatario(true);
        } else {
            if (esRemitente) {
                mensaje.setEliminadoPorRemitente(true);
            } else {
                mensaje.setEliminadoPorDestinatario(true);
            }
        }
        
        // Si ambos lo eliminaron, borrar físicamente
        if (mensaje.getEliminadoPorRemitente() && mensaje.getEliminadoPorDestinatario()) {
            mensajeRepository.delete(mensaje);
        } else {
            mensajeRepository.save(mensaje);
        }
    }
    
    private void validarLimiteVelocidad(Long userId) {
        long mensajesUltimoMinuto = mensajeRepository.contarMensajesUltimoMinuto(userId);
        if (mensajesUltimoMinuto >= 20) {
            throw new TooManyRequestsException("Límite de mensajes por minuto alcanzado");
        }
    }
    
    private String obtenerVistaPrevia(MensajePrivado mensaje) {
        switch (mensaje.getTipo()) {
            case TEXTO:
                return mensaje.getContenido().length() > 50 
                    ? mensaje.getContenido().substring(0, 50) + "..." 
                    : mensaje.getContenido();
            case AUDIO:
                return "🎤 Mensaje de audio (" + mensaje.getDuracionAudio() + "s)";
            case GIF:
                return "🖼️ GIF";
            case STICKER:
                return "😀 Sticker";
            case IMAGEN:
                return "📷 Imagen";
            default:
                return "Nuevo mensaje";
        }
    }
}
```

### Service: TransferenciaFichasService

```java
@Service
@RequiredArgsConstructor
public class TransferenciaFichasService {
    
    private final TransferenciaFichasRepository transferenciaRepository;
    private final UserRepository userRepository;
    private final AmistadRepository amistadRepository;
    private final NotificacionService notificacionService;
    private final WebSocketService webSocketService;
    private final AuditoriaService auditoriaService;
    
    private static final long TRANSFERENCIA_MINIMA = 100L;
    private static final long TRANSFERENCIA_MAXIMA = 10000L;
    private static final long LIMITE_DIARIO = 50000L;
    private static final int MAX_TRANSFERENCIAS_DIARIAS = 10;
    private static final double COMISION = 0.10; // 10%
    private static final int DIAS_MINIMOS_AMISTAD = 7;
    
    /**
     * Transfiere fichas entre amigos
     */
    @Transactional
    public TransferenciaFichasDTO transferirFichas(Long remitenteId, TransferirFichasDTO dto) {
        // Validaciones
        validarAmigos(remitenteId, dto.getDestinatarioId());
        validarAntiguedadAmistad(remitenteId, dto.getDestinatarioId());
        validarCantidad(dto.getCantidad());
        validarLimiteDiario(remitenteId, dto.getCantidad());
        validarCooldown(remitenteId);
        
        User remitente = userRepository.findById(remitenteId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        User destinatario = userRepository.findById(dto.getDestinatarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Validar que el remitente tiene fichas suficientes
        long comision = (long) (dto.getCantidad() * COMISION);
        long totalNecesario = dto.getCantidad() + comision;
        
        if (remitente.getFichas() < totalNecesario) {
            throw new BadRequestException(
                "Fichas insuficientes. Necesitas " + totalNecesario + 
                " fichas (" + dto.getCantidad() + " + " + comision + " de comisión)"
            );
        }
        
        // Realizar transferencia
        remitente.setFichas(remitente.getFichas() - totalNecesario);
        destinatario.setFichas(destinatario.getFichas() + dto.getCantidad());
        
        userRepository.save(remitente);
        userRepository.save(destinatario);
        
        // Registrar transferencia
        TransferenciaFichas transferencia = TransferenciaFichas.builder()
            .remitente(remitente)
            .destinatario(destinatario)
            .cantidad(dto.getCantidad())
            .mensaje(dto.getMensaje())
            .esRegalo(dto.getEsRegalo())
            .fecha(LocalDateTime.now())
            .estado(EstadoTransferencia.COMPLETADA)
            .build();
        
        transferencia = transferenciaRepository.save(transferencia);
        
        // Auditoría
        auditoriaService.registrarTransferencia(transferencia, comision);
        
        // Notificaciones
        notificacionService.enviarNotificacion(
            destinatario.getId(),
            TipoNotificacion.TRANSFERENCIA_RECIBIDA,
            "Fichas recibidas",
            remitente.getUsername() + " te envió " + dto.getCantidad() + " fichas",
            Map.of("transferenciaId", transferencia.getId())
        );
        
        // WebSocket
        TransferenciaFichasDTO transferenciaDTO = TransferenciaFichasDTO.fromEntity(transferencia);
        webSocketService.enviarAUsuario(
            destinatario.getId(),
            "/queue/amigos/transferencias",
            transferenciaDTO
        );
        
        return transferenciaDTO;
    }
    
    /**
     * Obtiene límites de transferencia del usuario
     */
    public LimitesTransferenciaDTO obtenerLimites(Long userId) {
        long usadoHoy = transferenciaRepository.sumarTransferenciasHoy(userId);
        int cantidadHoy = transferenciaRepository.contarTransferenciasHoy(userId);
        
        return LimitesTransferenciaDTO.builder()
            .limiteDiario(LIMITE_DIARIO)
            .usadoHoy(usadoHoy)
            .restanteHoy(LIMITE_DIARIO - usadoHoy)
            .limitePorTransferencia(TRANSFERENCIA_MAXIMA)
            .transferenciaMinima(TRANSFERENCIA_MINIMA)
            .transferenciasRealizadasHoy(cantidadHoy)
            .transferenciasRestantesHoy(MAX_TRANSFERENCIAS_DIARIAS - cantidadHoy)
            .comisionPorcentaje(COMISION * 100)
            .build();
    }
    
    // Validaciones
    
    private void validarAmigos(Long userId1, Long userId2) {
        if (!amistadRepository.existeAmistad(userId1, userId2)) {
            throw new ForbiddenException("Solo puedes transferir fichas a tus amigos");
        }
    }
    
    private void validarAntiguedadAmistad(Long userId1, Long userId2) {
        Amistad amistad = amistadRepository.findByUsuarios(userId1, userId2)
            .orElseThrow(() -> new ResourceNotFoundException("Amistad no encontrada"));
        
        long diasAmistad = ChronoUnit.DAYS.between(amistad.getFechaAmistad(), LocalDateTime.now());
        if (diasAmistad < DIAS_MINIMOS_AMISTAD) {
            throw new BadRequestException(
                "Deben ser amigos por al menos " + DIAS_MINIMOS_AMISTAD + " días para transferir fichas"
            );
        }
    }
    
    private void validarCantidad(long cantidad) {
        if (cantidad < TRANSFERENCIA_MINIMA) {
            throw new BadRequestException("La cantidad mínima es " + TRANSFERENCIA_MINIMA + " fichas");
        }
        if (cantidad > TRANSFERENCIA_MAXIMA) {
            throw new BadRequestException("La cantidad máxima es " + TRANSFERENCIA_MAXIMA + " fichas");
        }
    }
    
    private void validarLimiteDiario(Long userId, long cantidad) {
        long usadoHoy = transferenciaRepository.sumarTransferenciasHoy(userId);
        if (usadoHoy + cantidad > LIMITE_DIARIO) {
            throw new BadRequestException(
                "Límite diario excedido. Puedes transferir " + (LIMITE_DIARIO - usadoHoy) + " fichas más hoy"
            );
        }
        
        int cantidadHoy = transferenciaRepository.contarTransferenciasHoy(userId);
        if (cantidadHoy >= MAX_TRANSFERENCIAS_DIARIAS) {
            throw new BadRequestException("Has alcanzado el máximo de transferencias diarias");
        }
    }
    
    private void validarCooldown(Long userId) {
        LocalDateTime ultimaTransferencia = transferenciaRepository.findUltimaTransferencia(userId);
        if (ultimaTransferencia != null) {
            long minutosDesdeUltima = ChronoUnit.MINUTES.between(ultimaTransferencia, LocalDateTime.now());
            if (minutosDesdeUltima < 5) {
                throw new BadRequestException(
                    "Debes esperar " + (5 - minutosDesdeUltima) + " minutos antes de otra transferencia"
                );
            }
        }
    }
}
```

### Service: PresenciaService

```java
@Service
@RequiredArgsConstructor
public class PresenciaService {
    
    private final Map<Long, EstadoPresencia> estadosActivos = new ConcurrentHashMap<>();
    private final WebSocketService webSocketService;
    private final AmistadRepository amistadRepository;
    
    /**
     * Actualiza el estado de un usuario
     */
    public void actualizarEstado(Long userId, EstadoConexion nuevoEstado, String detalle) {
        EstadoPresencia estadoActual = estadosActivos.get(userId);
        
        EstadoPresencia nuevoEstadoPresencia = EstadoPresencia.builder()
            .userId(userId)
            .estado(nuevoEstado)
            .detalleEstado(detalle)
            .ultimaActividad(LocalDateTime.now())
            .aceptaInvitaciones(nuevoEstado != EstadoConexion.NO_MOLESTAR)
            .build();
        
        estadosActivos.put(userId, nuevoEstadoPresencia);
        
        // Notificar a amigos si cambió el estado
        if (estadoActual == null || !estadoActual.getEstado().equals(nuevoEstado)) {
            notificarCambioEstadoAAmigos(userId, nuevoEstadoPresencia);
        }
    }
    
    /**
     * Actualiza actividad del usuario (heartbeat)
     */
    public void actualizarActividad(Long userId) {
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setUltimaActividad(LocalDateTime.now());
            
            // Si estaba ausente y ahora hay actividad, cambiar a online
            if (estado.getEstado() == EstadoConexion.AUSENTE) {
                actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
            }
        }
    }
    
    /**
     * Usuario se conecta
     */
    public void conectar(Long userId) {
        actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
        notificarConexionAAmigos(userId);
    }
    
    /**
     * Usuario se desconecta
     */
    public void desconectar(Long userId) {
        actualizarEstado(userId, EstadoConexion.OFFLINE, "Desconectado");
        estadosActivos.remove(userId);
        notificarDesconexionAAmigos(userId);
    }
    
    /**
     * Usuario entra a una partida
     */
    public void entrarAPartida(Long userId, Long mesaId, String nombreMesa) {
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setEstado(EstadoConexion.EN_PARTIDA);
            estado.setDetalleEstado("Jugando en " + nombreMesa);
            estado.setMesaId(mesaId);
            notificarCambioEstadoAAmigos(userId, estado);
        }
    }
    
    /**
     * Usuario sale de una partida
     */
    public void salirDePartida(Long userId) {
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setEstado(EstadoConexion.ONLINE);
            estado.setDetalleEstado("En línea");
            estado.setMesaId(null);
            notificarCambioEstadoAAmigos(userId, estado);
        }
    }
    
    /**
     * Obtiene el estado de un usuario
     */
    public EstadoPresencia obtenerEstado(Long userId) {
        return estadosActivos.getOrDefault(
            userId, 
            EstadoPresencia.builder()
                .userId(userId)
                .estado(EstadoConexion.OFFLINE)
                .detalleEstado("Desconectado")
                .build()
        );
    }
    
    /**
     * Verifica si un usuario está conectado
     */
    public boolean estaConectado(Long userId) {
        EstadoPresencia estado = estadosActivos.get(userId);
        return estado != null && estado.getEstado() != EstadoConexion.OFFLINE;
    }
    
    /**
     * Obtiene estados de todos los amigos de un usuario
     */
    public List<EstadoPresencia> obtenerEstadosAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        return amigosIds.stream()
            .map(this::obtenerEstado)
            .collect(Collectors.toList());
    }
    
    private void notificarCambioEstadoAAmigos(Long userId, EstadoPresencia estado) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        
        // Verificar configuración de privacidad y notificaciones
        for (Long amigoId : amigosIds) {
            if (debeNotificar(userId, amigoId)) {
                webSocketService.enviarAUsuario(
                    amigoId,
                    "/queue/amigos/estados",
                    Map.of(
                        "tipo", "CAMBIO_ESTADO",
                        "userId", userId,
                        "estado", estado.getEstado(),
                        "detalleEstado", estado.getDetalleEstado()
                    )
                );
            }
        }
    }
    
    private void notificarConexionAAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        String username = obtenerUsername(userId);
        
        for (Long amigoId : amigosIds) {
            if (debeNotificarConexion(userId, amigoId)) {
                webSocketService.enviarAUsuario(
                    amigoId,
                    "/queue/amigos/estados",
                    Map.of(
                        "tipo", "AMIGO_CONECTADO",
                        "userId", userId,
                        "username", username
                    )
                );
            }
        }
    }
    
    private void notificarDesconexionAAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        String username = obtenerUsername(userId);
        
        for (Long amigoId : amigosIds) {
            if (debeNotificar(userId, amigoId)) {
                webSocketService.enviarAUsuario(
                    amigoId,
                    "/queue/amigos/estados",
                    Map.of(
                        "tipo", "AMIGO_DESCONECTADO",
                        "userId", userId,
                        "username", username
                    )
                );
            }
        }
    }
    
    private boolean debeNotificar(Long userId, Long amigoId) {
        Amistad amistad = amistadRepository.findByUsuarios(userId, amigoId).orElse(null);
        if (amistad == null) return false;
        
        return amistad.getUsuario1().getId().equals(amigoId) 
            ? amistad.getNotificacionesActivas1() 
            : amistad.getNotificacionesActivas2();
    }
    
    private boolean debeNotificarConexion(Long userId, Long amigoId) {
        // Verificar configuración de privacidad del usuario
        ConfiguracionPrivacidad config = configRepository.findByUserId(userId).orElse(null);
        return config != null && config.getNotificarConexion() && debeNotificar(userId, amigoId);
    }
    
    /**
     * Job para detectar usuarios inactivos (AFK)
     */
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    public void detectarUsuariosInactivos() {
        LocalDateTime umbralInactividad = LocalDateTime.now().minusMinutes(10);
        
        estadosActivos.entrySet().stream()
            .filter(entry -> {
                EstadoPresencia estado = entry.getValue();
                return estado.getEstado() == EstadoConexion.ONLINE 
                    && estado.getUltimaActividad().isBefore(umbralInactividad);
            })
            .forEach(entry -> {
                actualizarEstado(entry.getKey(), EstadoConexion.AUSENTE, "Ausente");
            });
    }
}
```

---

## 📱 Ejemplo de implementación en Frontend

### React: Componente de Lista de Amigos

```jsx
import React, { useState, useEffect } from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import { amigosService } from './services/amigosService';

const ListaAmigos = () => {
  const [amigos, setAmigos] = useState([]);
  const [filtro, setFiltro] = useState('todos');
  const { subscribe, unsubscribe } = useWebSocket();

  useEffect(() => {
    cargarAmigos();
    
    // Suscribirse a actualizaciones de estado
    const subscriptionId = subscribe('/user/queue/amigos/estados', (mensaje) => {
      manejarActualizacionEstado(mensaje);
    });

    return () => unsubscribe(subscriptionId);
  }, [filtro]);

  const cargarAmigos = async () => {
    const data = await amigosService.obtenerAmigos({ filtro });
    setAmigos(data);
  };

  const manejarActualizacionEstado = (mensaje) => {
    switch (mensaje.tipo) {
      case 'AMIGO_CONECTADO':
        mostrarNotificacion(`${mensaje.username} se conectó`);
        cargarAmigos();
        break;
      case 'CAMBIO_ESTADO':
        actualizarEstadoAmigo(mensaje.userId, mensaje.estado, mensaje.detalleEstado);
        break;
    }
  };

  const actualizarEstadoAmigo = (userId, nuevoEstado, detalle) => {
    setAmigos(prevAmigos => 
      prevAmigos.map(amigo => 
        amigo.userId === userId 
          ? { ...amigo, estado: nuevoEstado, detalleEstado: detalle }
          : amigo
      )
    );
  };

  const invitarAPartida = async (amigoId) => {
    try {
      await amigosService.invitarAPartida({
        destinatarioId: amigoId,
        mesaId: mesaActual.id,
        tipo: 'JUGADOR'
      });
      mostrarNotificacion('Invitación enviada');
    } catch (error) {
      mostrarError(error.message);
    }
  };

  return (
    <div className="lista-amigos">
      <div className="filtros">
        <button onClick={() => setFiltro('online')}>En línea</button>
        <button onClick={() => setFiltro('favoritos')}>Favoritos</button>
        <button onClick={() => setFiltro('todos')}>Todos</button>
      </div>

      <div className="amigos">
        {amigos.map(amigo => (
          <div key={amigo.userId} className="amigo-card">
            <img src={amigo.avatarUrl} alt={amigo.username} />
            <div className="info">
              <h3>{amigo.alias || amigo.username}</h3>
              <span className={`estado ${amigo.estado.toLowerCase()}`}>
                {getIconoEstado(amigo.estado)} {amigo.detalleEstado}
              </span>
              <p>Fichas: {amigo.fichas.toLocaleString()}</p>
            </div>
            <div className="acciones">
              <button onClick={() => abrirChat(amigo.userId)}>
                💬 Chat
              </button>
              {amigo.puedeUnirse && (
                <button onClick={() => invitarAPartida(amigo.userId)}>
                  🎮 Invitar
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const getIconoEstado = (estado) => {
  switch (estado) {
    case 'ONLINE': return '🟢';
    case 'EN_PARTIDA': return '🎮';
    case 'EN_TORNEO': return '🏆';
    case 'AUSENTE': return '⏸️';
    case 'NO_MOLESTAR': return '🚫';
    case 'OFFLINE': return '🔴';
    default: return '';
  }
};
```

### React: Componente de Chat Privado

```jsx
import React, { useState, useEffect, useRef } from 'react';
import { chatService } from './services/chatService';
import { useWebSocket } from './hooks/useWebSocket';

const ChatPrivado = ({ amigoId }) => {
  const [mensajes, setMensajes] = useState([]);
  const [textoMensaje, setTextoMensaje] = useState('');
  const [grabandoAudio, setGrabandoAudio] = useState(false);
  const chatRef = useRef(null);
  const { subscribe } = useWebSocket();

  useEffect(() => {
    cargarMensajes();
    marcarComoLeidos();

    // Suscribirse a nuevos mensajes
    const subscriptionId = subscribe('/user/queue/chat/mensajes', (mensaje) => {
      if (mensaje.remitenteId === amigoId) {
        setMensajes(prev => [...prev, mensaje]);
        marcarComoLeidos();
        scrollToBottom();
      }
    });

    return () => unsubscribe(subscriptionId);
  }, [amigoId]);

  const cargarMensajes = async () => {
    const data = await chatService.obtenerConversacion(amigoId, { page: 0, size: 50 });
    setMensajes(data.content);
    scrollToBottom();
  };

  const enviarMensaje = async () => {
    if (!textoMensaje.trim()) return;

    try {
      const mensaje = await chatService.enviarMensaje({
        destinatarioId: amigoId,
        tipo: 'TEXTO',
        contenido: textoMensaje
      });
      
      setMensajes(prev => [...prev, mensaje]);
      setTextoMensaje('');
      scrollToBottom();
    } catch (error) {
      mostrarError(error.message);
    }
  };

  const enviarGif = async (gifUrl) => {
    const mensaje = await chatService.enviarMensaje({
      destinatarioId: amigoId,
      tipo: 'GIF',
      contenido: gifUrl
    });
    
    setMensajes(prev => [...prev, mensaje]);
    scrollToBottom();
  };

  const grabarAudio = async () => {
    if (!grabandoAudio) {
      // Iniciar grabación
      setGrabandoAudio(true);
      // Lógica de grabación...
    } else {
      // Detener y enviar
      setGrabandoAudio(false);
      const audioBlob = await detenerGrabacion();
      const audioUrl = await chatService.uploadAudio(audioBlob);
      
      const mensaje = await chatService.enviarMensaje({
        destinatarioId: amigoId,
        tipo: 'AUDIO',
        contenido: audioUrl,
        duracionAudio: 15
      });
      
      setMensajes(prev => [...prev, mensaje]);
      scrollToBottom();
    }
  };

  const marcarComoLeidos = async () => {
    await chatService.marcarComoLeidos(amigoId);
  };

  const scrollToBottom = () => {
    if (chatRef.current) {
      chatRef.current.scrollTop = chatRef.current.scrollHeight;
    }
  };

  return (
    <div className="chat-privado">
      <div className="mensajes" ref={chatRef}>
        {mensajes.map(mensaje => (
          <div 
            key={mensaje.id} 
            className={`mensaje ${mensaje.esPropio ? 'propio' : 'ajeno'}`}
          >
            {mensaje.tipo === 'TEXTO' && (
              <p>{mensaje.contenido}</p>
            )}
            {mensaje.tipo === 'AUDIO' && (
              <audio controls src={mensaje.contenido} />
            )}
            {mensaje.tipo === 'GIF' && (
              <img src={mensaje.contenido} alt="GIF" />
            )}
            <span className="hora">
              {formatearHora(mensaje.fechaEnvio)}
              {mensaje.leido && <span className="leido">✓✓</span>}
            </span>
          </div>
        ))}
      </div>

      <div className="input-area">
        <button onClick={() => abrirSelectorGif()}>GIF</button>
        <button onClick={grabarAudio}>
          {grabandoAudio ? '⏹️' : '🎤'}
        </button>
        <input
          type="text"
          value={textoMensaje}
          onChange={(e) => setTextoMensaje(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && enviarMensaje()}
          placeholder="Escribe un mensaje..."
        />
        <button onClick={enviarMensaje}>Enviar</button>
      </div>
    </div>
  );
};
```

---

## 🧪 Testing

### Test de AmigosService

```java
@SpringBootTest
class AmigosServiceTest {
    
    @Autowired
    private AmigosService amigosService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void debeEnviarSolicitudCorrectamente() {
        // Given
        User usuario1 = crearUsuario("user1");
        User usuario2 = crearUsuario("user2");
        
        // When
        SolicitudAmistadDTO solicitud = amigosService.enviarSolicitud(
            usuario1.getId(), 
            usuario2.getId(), 
            "Hola!"
        );
        
        // Then
        assertNotNull(solicitud);
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
    }
    
    @Test
    void noDebePermitirSolicitudDuplicada() {
        // Given
        User usuario1 = crearUsuario("user1");
        User usuario2 = crearUsuario("user2");
        amigosService.enviarSolicitud(usuario1.getId(), usuario2.getId(), "Hola!");
        
        // When & Then
        assertThrows(BadRequestException.class, () -> {
            amigosService.enviarSolicitud(usuario1.getId(), usuario2.getId(), "Hola de nuevo!");
        });
    }
    
    @Test
    void debeAceptarSolicitudYCrearAmistad() {
        // Given
        User usuario1 = crearUsuario("user1");
        User usuario2 = crearUsuario("user2");
        SolicitudAmistadDTO solicitud = amigosService.enviarSolicitud(
            usuario1.getId(), 
            usuario2.getId(), 
            null
        );
        
        // When
        AmistadDTO amistad = amigosService.aceptarSolicitud(solicitud.getId(), usuario2.getId());
        
        // Then
        assertNotNull(amistad);
        assertTrue(amigosService.sonAmigos(usuario1.getId(), usuario2.getId()));
    }
}
```

---

## 🔐 Seguridad y anti-fraude

### Medidas implementadas

1. **Prevención de spam:**
    - Límite de solicitudes diarias
    - Cooldown entre acciones
    - Throttling de mensajes

2. **Anti-fraude en transferencias:**
    - Días mínimos de amistad (7 días)
    - Comisión del 10%
    - Límites diarios
    - Registro completo de auditoría
    - Reversión por admins

3. **Moderación de contenido:**
    - Filtro de palabras ofensivas
    - Sistema de reportes
    - Bloqueo automático tras múltiples reportes

4. **Protección de datos:**
    - Los bloqueados no ven tu información
    - Configuración granular de privacidad
    - Modo invisible

---

## 📊 Métricas y analytics

### KPIs sugeridos

- Tasa de aceptación de solicitudes
- Tiempo promedio de respuesta a solicitudes
- Mensajes enviados por día
- Transferencias de fichas por día
- Tasa de conversión de invitaciones
- Retención de usuarios con amigos vs sin amigos

---

## 🚀 Roadmap de mejoras

### Fase 1 (Completado)
- ✅ Gestión básica de amistades
- ✅ Chat privado con texto y audio
- ✅ Transferencia de fichas
- ✅ Sistema de presencia

### Fase 2 (En desarrollo)
- 🔄 Chat grupal
- 🔄 Videollamadas entre amigos
- 🔄 Compartir replays de partidas
- 🔄 Regalos y emoticones premium

### Fase 3 (Planificado)
- 📅 Sistema de clanes/grupos
- 📅 Torneos privados entre amigos
- 📅 Apuestas amistosas
- 📅 Logros compartidos
- 📅 Tabla de clasificación de amigos

---

## 🎓 Conclusión

El sistema de amigos proporciona una capa social completa que aumenta significativamente el engagement y la retención de usuarios. Las funcionalidades de chat, transferencias e invitaciones crean una experiencia más social y divertida, incentivando a los jugadores a invitar amigos y jugar juntos.

**Beneficios clave:**
- Mayor retención de usuarios
- Incremento en tiempo de sesión
- Viralidad orgánica (invitaciones)
- Comunidad más fuerte
- Monetización adicional (fichas premium para regalos)

---

*Última actualización: 01 de octubre de 2025*