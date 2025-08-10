# Challenge-LiterAlura
Challenge de uso de API Gutendex con APP Java

Aplicación **de consola** en Java/Spring Boot para buscar libros y autores en la API pública de **Gutendex**, guardarlos en **PostgreSQL** y hacer consultas rápidas (listar, filtrar, autores vivos en un año, etc.).  
Incluye manejo de duplicados, guardado masivo (“a = todos”), relación muchos-a-muchos libro–autor y salida formateada.

## Características

- 🔎 **Buscar libros por título** (permite guardar 1/varios/todos).
- 🖋️ **Buscar autores por nombre** (permite guardar 1/varios/todos).
- 📚 **Listar libros registrados** (muestra autores y idioma).
- 👤 **Listar autores registrados** (muestra años y nº de libros).
- 🧭 **Autores vivos en un año** (indica cuáles estaban vivos).
- 🚪 **Salir** (opción 8).
- Menú con **iconos** y entradas limpias (sin ruido de logs).
- Manejo de **duplicados** inteligente (case-insensitive).
- **LazyInitialization** y **detached entity** solucionados.
- Acepta títulos largos (columna `titulo` en **TEXT**).
- Cliente HTTP con `Accept: application/json` para evitar 406.

## Requisitos

- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 13+**
- Acceso a Internet (para llamar a Gutendex)

## Configuración de la base de datos

Crea la base (puedes hacerlo con pgAdmin o `psql`):

- **Host:** `127.0.1.0`  
- **Puerto:** `5432`  
- **DB:** `gutendex`  
- **Usuario:** `postgres`  
- **Password:** `admin`

> Ajusta estos valores si en tu entorno son distintos.

### `application.properties` (ejemplo)

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://127.0.1.0:5432/gutendex
spring.datasource.username=postgres
spring.datasource.password=admin

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Reducir ruido en consola
logging.level.org.hibernate=WARN
logging.level.org.springframework.web=INFO
```

### Migración rápida (si vienes de una DB previa)

**1) Aceptar títulos largos** (evita “value too long for type character varying(500)”):

```sql
BEGIN;
ALTER TABLE public.libro
  ALTER COLUMN titulo TYPE TEXT;
COMMIT;
```

**2) (Sólo si tu DB venía con problemas de secuencias/id)**  
Fija las secuencias para que no fallen inserts:

```sql
BEGIN;
SELECT setval('public.libro_id_seq', COALESCE((SELECT MAX(id) FROM public.libro), 0) + 1, false);
SELECT setval('public.autor_id_seq', COALESCE((SELECT MAX(id) FROM public.autor), 0) + 1, false);
COMMIT;
```

> Si es una instalación limpia, con `ddl-auto=update` normalmente no necesitas estos pasos.

## Compilar y ejecutar

Desde la raíz del proyecto:

```bash
mvn clean package -DskipTests
java -jar target/gutendex-console-*.jar
```

O directamente:

```bash
mvn spring-boot:run
```

## Uso

Al iniciar verás el menú:

```
===============================
  📚 Gutendex Console
===============================
1) 🔎 Buscar libros por título
2) 🖋️ Buscar autores por nombre
3) 📚 Listar libros registrados
4) 👤 Listar autores registrados
7) 🧭 Autores vivos en un año
8) 🚪 Salir
-------------------------------
Elige una opción:
```

### Guardar resultados de búsquedas
Cuando buscas por **título** o **autor**, la app muestra los hallazgos y pregunta:

```
¿Guardar? [n = ninguno, número = índice, a = todos]:
```

- Escribe **`a`** para guardar **todos**.
- Escribe **un número** para guardar **sólo ese índice**.
- Escribe **`n`** para no guardar ninguno.

La app evita duplicados: si ya existe el libro/autor (match por nombre/título, insensible a mayúsculas), no lo vuelve a crear.

## Detalles técnicos

- **Stack:** Spring Boot 3.1.x, Spring Data JPA (Hibernate), RestTemplate, PostgreSQL.
- **Entidades:**
  - `Libro` (`id` por **SEQUENCE**, `titulo` **TEXT**, `idioma`, `descargas`, relación `@ManyToMany` con `Autor`).
  - `Autor` (`id` por **SEQUENCE**, `nombre`, `anioNacimiento`, `anioFallecimiento`).
- **Relación:** tabla intermedia `libro_autor`.
- **Cliente Gutendex:** se fuerza `Accept: application/json` para evitar **406 Not Acceptable**.
- **Transacciones y Lazy:** servicios usan `@Transactional` en operaciones de guardado/consulta que requieren inicializar relaciones. Se evitan `detached entity` consultando/autogestionando autores antes de asociarlos.
- **Logs limpios:** niveles reducidos en `application.properties`.

## Solución de problemas

- **`FATAL: password authentication failed for user "postgres"`**  
  Verifica usuario/password/host/puerto en `application.properties` y tu servidor PostgreSQL.

- **`psql: command not found` (Windows PowerShell)**  
  Agrega la carpeta `bin` de PostgreSQL al **PATH** o usa **pgAdmin** para ejecutar SQL.

- **`406 Not Acceptable` al consultar Gutendex**  
  Ya está corregido en el cliente HTTP (encabezado `Accept: application/json`).

- **`value too long for type character varying(500)`**  
  Asegúrate de que has corrido el `ALTER COLUMN titulo TYPE TEXT` (ver sección “Migración rápida”).

- **`LazyInitializationException` o `detached entity passed to persist`**  
  Ya está resuelto en esta versión (transacciones y gestión de entidades). Si tocas el código, mantén `@Transactional` en los métodos de servicio que leen y luego persisten relaciones.

## Estructura del proyecto (resumen)

```
src/main/java/com/gutendex/
├── client/
│   └── GutendexClient.java        # RestTemplate + Accept: application/json
├── dto/                           # DTOs para parsear Gutendex
├── entity/
│   ├── Autor.java
│   └── Libro.java                 # titulo TEXT, relación M:N
├── menu/
│   └── MenuService.java           # Menú con íconos, opción 8 = salir
├── repository/
│   ├── AutorRepository.java
│   └── LibroRepository.java
└── service/
    ├── AutorService.java
    └── LibroService.java          # Guardar “a/n/índice”, duplicados, transacciones
```

## Licencia

Este proyecto se distribuye bajo la licencia **MIT**. Úsalo y modifícalo libremente.
