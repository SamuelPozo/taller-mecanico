# 🔧 TallerMec — Sistema de Gestión de Taller Mecánico

> **Proyecto Intermodular — Desarrollo de Aplicaciones Multiplataforma**
> IES Politécnico Hermenegildo Lanz, Granada · Curso 2025/2026
> Alumno: Samuel Pozo Olvera

---

## 📋 Descripción

TallerMec es un sistema integral de gestión para talleres mecánicos compuesto por **tres aplicaciones interconectadas** que comparten los mismos datos en tiempo real a través de una API REST segura.

| Aplicación | Tecnología | Destinatario |
|---|---|---|
| **App de Escritorio** | JavaFX 21 + Java 21 | Administrador del taller |
| **App Móvil** | React Native + Expo SDK 54 + TypeScript | Mecánicos y Clientes |
| **API REST Backend** | Spring Boot 3.5 + Java 24 + MySQL | Capa de negocio común |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────┐
│           CAPA DE PRESENTACIÓN              │
│   JavaFX (Admin)  ·  React Native (Móvil)  │
└──────────────┬──────────────────────────────┘
               │ HTTP · JSON · JWT
┌──────────────▼──────────────────────────────┐
│            CAPA DE NEGOCIO                  │
│   Spring Boot REST API · Spring Security    │
│   JWT Auth · JPA/Hibernate · Scheduler      │
└──────────────┬──────────────────────────────┘
               │ JPA / SQL
┌──────────────▼──────────────────────────────┐
│             CAPA DE DATOS                   │
│        MySQL 8 · 12 entidades               │
└─────────────────────────────────────────────┘
```

---

## 📁 Estructura del Repositorio

```
taller-mecanico/
├── taller-backend/          # API REST Spring Boot
├── taller-desktop/          # Aplicación de escritorio JavaFX
├── taller-mobile/           # Aplicación móvil React Native
├── database/
│   └── taller_mecanico.sql  # Script completo BD (estructura + datos de prueba)
├── docs/
│   ├── Documentacion_TFG.docx
│   ├── Manual_Usuario.docx
│   ├── Presentacion_Defensa.pptx
│   └── Guion_Defensa.docx
└── instaladores/
    ├── TallerMecanico-1.0.msi   # Instalador Windows (app escritorio)
    └── TallerMec.apk            # APK Android (app móvil)
```

---

## ⚙️ Requisitos Previos

| Herramienta | Versión | Uso |
|---|---|---|
| Java JDK | 24 o superior | Backend Spring Boot |
| Java JDK | 21 o superior | App de escritorio JavaFX |
| Node.js | 18 o superior | App móvil React Native |
| MySQL | 8.x | Base de datos |
| XAMPP | 8.x | Servidor local MySQL + phpMyAdmin |
| Android Studio | 2024.x | Emulador Android (para desarrollo) |
| IntelliJ IDEA | 2024.x | Desarrollo backend y escritorio |
| Visual Studio Code | 1.9x | Desarrollo móvil |

---

## 🚀 Instrucciones de Instalación y Arranque

### 1️⃣ Base de Datos

1. Inicia **XAMPP** y activa el servicio **MySQL**
2. Accede a **phpMyAdmin** en `http://localhost/phpmyadmin`
3. Crea una base de datos con el nombre: `taller_mecanico`
4. Importa el archivo `database/taller_mecanico.sql`
5. La base de datos quedará lista con estructura y datos de prueba

### 2️⃣ Backend (Spring Boot)

1. Abre la carpeta `taller-backend` en **IntelliJ IDEA**
2. Verifica la configuración en `src/main/resources/application.yaml`:
   ```yaml
   datasource:
     url: jdbc:mysql://localhost:3306/taller_mecanico
     username: root
     password:        # vacío por defecto en XAMPP
   ```
3. Ejecuta la clase principal: `TallerBackendApplication.java`
4. El servidor arranca en: `http://localhost:8080`

> ⚠️ **Importante:** Ejecutar desde IntelliJ directamente, NO mediante Maven.

### 3️⃣ Crear el Primer Administrador

Solo es necesario la primera vez. Envía esta petición con Postman o cualquier cliente HTTP:

```http
POST http://localhost:8080/api/admin/setup
Content-Type: application/json

{
  "nombre": "Admin",
  "apellidos": "Taller",
  "email": "admin@taller.com",
  "password": "admin123"
}
```

> ℹ️ Este endpoint se bloquea automáticamente tras la primera ejecución.
> Si importaste el SQL de prueba, el administrador **ya está creado**.

### 4️⃣ Aplicación de Escritorio (JavaFX)

**Opción A — Usando el instalador (recomendado):**
1. Ejecuta `instaladores/TallerMecanico-1.0.msi`
2. Sigue el asistente de instalación
3. Abre TallerMec desde el acceso directo del escritorio

**Opción B — Desde el código fuente:**
1. Abre `taller-desktop` en IntelliJ IDEA
2. Asegúrate de que el backend está corriendo
3. Ejecuta `MainApp.java`

### 5️⃣ Aplicación Móvil (React Native)

**Opción A — Usando el APK (recomendado):**
1. Instala `instaladores/TallerMec.apk` en un dispositivo Android o emulador
2. Abre la app y accede con las credenciales de prueba

**Opción B — Desde el código fuente:**
```bash
cd taller-mobile
npm install
npx expo start
# Pulsa 'a' para abrir en el emulador Android
```

> ⚠️ La URL base de la API está configurada como `http://10.0.2.2:8080/api`
> (dirección del localhost desde el emulador Android).
> Si usas un dispositivo físico, cambia la IP en `src/api/api.ts`.

---

## 🔑 Credenciales de Prueba

| Usuario | Email | Contraseña | Rol |
|---|---|---|---|
| Admin Taller | admin@taller.com | admin123 | Administrador |
| Carlos García | carlos@taller.com | admin123 | Mecánico |
| Pedro Martínez | pedro@taller.com | admin123 | Mecánico |
| Ana Fernández | ana@taller.com | admin123 | Mecánico |
| Luis Sánchez | luis@cliente.com | admin123 | Cliente |
| María López | maria@cliente.com | admin123 | Cliente |
| José Romero | jose@cliente.com | admin123 | Cliente |
| Elena Jiménez | elena@cliente.com | admin123 | Cliente |

---

## 🗄️ Base de Datos — Entidades

El sistema cuenta con **12 entidades** relacionadas:

```
USUARIO ──┬── MECANICO ──┬── HORARIO
          │              ├── AUSENCIA
          │              └── ORDEN_TRABAJO ──┬── HISTORIAL_ESTADO
          │                                  └── NOTIFICACION
          └── CLIENTE ──── VEHICULO
                           
PLAZA ──── ESTACIONAMIENTO
SERVICIO
```

---

## 🔒 Seguridad

- Autenticación mediante **JWT (JSON Web Tokens)** con expiración de 24 horas
- Contraseñas hasheadas con **BCrypt**
- Control de acceso por roles en cada endpoint:
  - `/api/auth/**` — Público
  - `/api/admin/setup` — Público (autobloqueante tras primer uso)
  - `/api/admin/**` — Solo ADMIN
  - `/api/mecanico/**` — MECANICO y ADMIN
  - `/api/cliente/**` — CLIENTE y ADMIN
  - `/api/notificaciones/**` — Cualquier usuario autenticado

---

## 📡 API REST — Endpoints Principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Login — devuelve token JWT |
| POST | `/api/auth/register` | Registro de clientes |
| PATCH | `/api/auth/foto-perfil` | Actualizar foto de perfil (Base64) |
| GET/POST | `/api/admin/mecanicos` | CRUD mecánicos |
| GET/POST | `/api/admin/clientes` | CRUD clientes |
| GET/POST | `/api/admin/vehiculos` | CRUD vehículos |
| GET/POST | `/api/admin/ordenes` | CRUD órdenes de trabajo |
| PATCH | `/api/admin/ordenes/{id}/estado` | Cambiar estado de una orden |
| GET/POST | `/api/admin/horarios` | CRUD horarios |
| GET/PATCH | `/api/admin/ausencias` | Gestión de ausencias |
| GET/POST | `/api/admin/plazas` | CRUD plazas |
| GET | `/api/mecanico/ordenes/{mecanicoId}` | Órdenes del mecánico |
| PATCH | `/api/mecanico/ordenes/{id}/diagnostico` | Actualizar diagnóstico |
| GET/POST | `/api/mecanico/ausencias` | Ausencias del mecánico |
| GET | `/api/cliente/ordenes/{clienteId}` | Órdenes del cliente |
| GET | `/api/notificaciones` | Notificaciones del usuario |
| GET | `/api/notificaciones/no-leidas` | Contador de no leídas |

---

## 🔔 Sistema de Notificaciones Automáticas

El sistema genera notificaciones automáticamente ante los siguientes eventos:

| Evento | Destinatario |
|---|---|
| Crear orden de trabajo | Mecánico asignado + Cliente |
| Cambiar estado de orden | Mecánico + Cliente |
| Actualizar diagnóstico | Cliente |
| Crear/eliminar horario | Mecánico |
| Solicitar ausencia | Todos los administradores |
| Aprobar/rechazar ausencia | Mecánico solicitante |
| Scheduler diario 8:00h | Admin (ausencias próximas 1-2 días) |

---

## 🛠️ Stack Tecnológico Completo

### Backend
- Spring Boot 3.5 · Java 24
- Spring Security + JJWT 0.12.6
- Spring Data JPA + Hibernate
- MySQL 8 · Maven

### Escritorio
- JavaFX 21.0.6 · Java 21
- OkHttp3 4.12.0 · Jackson 2.17.0
- ControlsFX 11.2.1 · Maven

### Móvil
- React Native · Expo SDK 54
- TypeScript · React Navigation
- Axios · AsyncStorage
- expo-image-picker

---

## 📄 Licencia

Proyecto académico desarrollado para el Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma.

IES Politécnico Hermenegildo Lanz · Granada · 2026
