# 🌱 backendHuerto · Microservicio Spring Boot

Microservicio REST del **Huerto Escolar** (DSY1105 · Duoc UC). Gestiona **sectores** y **cultivos** y los guarda en **MySQL/MariaDB (XAMPP)**.
La app Android (repo **huerto-escolar**) lo consume con Retrofit.

## 🛠️ Tecnologías
Java 17 · Spring Boot 3.3 · Spring Web · Spring Data JPA (Hibernate) · Bean Validation · MariaDB/MySQL (XAMPP) · JUnit 5 + Mockito

## 🗂️ Estructura (arquitectura en capas)
```
cl.duoc.huertoapi
├── HuertoApiApplication.java    → punto de entrada
├── controller/                  → endpoints REST (reciben la petición HTTP)
├── service/                     → reglas de negocio
├── repository/                  → acceso a la base de datos (Spring Data JPA)
├── model/                       → entidades JPA (tablas): Sector, Cultivo y sus estados
├── dto/                         → datos que viajan en JSON (con validaciones)
├── exception/                   → errores y respuestas HTTP (404, 409, 400)
└── config/DatosIniciales.java   → carga los datos de prueba si la base está vacía
```
**Flujo:** `App → Controller → Service → Repository → MySQL`

## 🧱 Entidades y relación
| Tabla | Campos principales |
|---|---|
| `sectores` | id, codigo (único, `S-01`), nombre, descripcion, ubicacion, estado, responsable_id, fechas |
| `cultivos` | id, nombre, descripcion, **sector_id (FK → sectores)**, fecha_siembra, fecha_trasplante, fecha_cosecha, frecuencia_riego_dias, estado, observaciones |

Relación: **un sector tiene muchos cultivos** (`@ManyToOne` en `Cultivo`).

## 🔗 Endpoints
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/sectores` | Lista los sectores |
| GET | `/api/sectores/{id}` | Un sector |
| GET | `/api/sectores/{id}/cultivos` | Cultivos de un sector |
| POST | `/api/sectores` | Crea un sector |
| PUT | `/api/sectores/{id}` | Modifica un sector |
| DELETE | `/api/sectores/{id}` | Elimina (no se puede si tiene cultivos) |
| GET | `/api/cultivos` | Lista los cultivos |
| GET | `/api/cultivos/{id}` | Un cultivo |
| POST | `/api/cultivos` | Crea un cultivo |
| PUT | `/api/cultivos/{id}` | Modifica un cultivo |
| DELETE | `/api/cultivos/{id}` | Elimina un cultivo |

Ejemplo de JSON (POST `/api/sectores`):
```json
{ "codigo": "S-05", "nombre": "Bancal Sur", "descripcion": "Nuevo bancal", "ubicacion": "Patio",
  "estado": "DISPONIBLE", "responsableId": 1, "fechaCreacion": "21/11/2026", "fechaActualizacion": "21/11/2026" }
```

## ✅ Reglas y respuestas de error
| Regla | Respuesta |
|---|---|
| Campos obligatorios, formato `S-01`, fechas `dd/mm/aaaa`, riego entre 1 y 30 | 400 Bad Request (con el error de cada campo) |
| Código de sector repetido | 409 Conflict |
| Cosecha o trasplante antes de la siembra | 409 Conflict |
| Eliminar un sector con cultivos | 409 Conflict |
| Id que no existe | 404 Not Found |

## ▶️ Cómo ejecutar
1. Abrir **XAMPP** y presionar **Start** en **MySQL** (puerto 3306, usuario `root` sin clave).
2. Abrir esta carpeta en **IntelliJ IDEA** (abrir el `pom.xml` como proyecto) y esperar que Maven descargue las dependencias.
3. Si pide JDK: **File → Project Structure → SDK → Download JDK → versión 17**.
4. Ejecutar `HuertoApiApplication` (▶ verde).
5. Probar en el navegador: http://localhost:8080/api/sectores

La base `huerto_db` y sus tablas se crean solas (`ddl-auto=update`). Se ven en phpMyAdmin: http://localhost/phpmyadmin

## 🧪 Pruebas unitarias
`src/test/java/cl/duoc/huertoapi/service/` → clic derecho → **Run Tests**. Usan Mockito, así que **no necesitan MySQL encendido**.
| Archivo | Qué prueba |
|---|---|
| `SectorServiceTest` | Crear sector, código repetido, eliminar sector con cultivos, id inexistente |
| `CultivoServiceTest` | Cosecha antes de la siembra, fecha imposible, sector inexistente, crear cultivo válido |
