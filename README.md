# Foro-HUB (Spring Boot + JWT + MySQL)

API REST de un foro con **autenticación JWT Bearer**, **CRUD de tópicos**, paginación, validaciones y migraciones con **Flyway**.

## 🧰 Stack
- **Java** 21
- **Spring Boot** 3.5.4
  - Web, Security, Data JPA, Validation
- **MySQL** 8.0
- **Flyway** (core + `flyway-mysql`)
- **JWT** (jjwt 0.11.5)
- **Maven Wrapper** (`mvnw` / `mvnw.cmd`)
- Probado con **Insomnia** (colección incluida más abajo).

---

## 🏗️ Arquitectura (resumen)
- `auth/`: registro y login, emisión de **JWT** (HS256).
- `security/`: `SecurityConfig`, `JwtAuthFilter`, `CustomUserDetailsService`, `PasswordEncoder`.
- `user/`: entidad `User` + repositorio.
- `topics/`: entidad `Topic`, repositorio, controlador REST + DTOs de request/response y validaciones.
- `resources/db/migration/`: **Flyway** con `V1__init.sql` (creación de tablas base).

---

## ⚙️ Configuración

### 1) Base de datos
Crea una base en MySQL:
```sql
CREATE DATABASE foro_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2) `application.yml` (ejemplo)
> Ajusta usuario/clave de MySQL y el secreto del JWT.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/foro_hub?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate        # usamos Flyway para migraciones
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true

app:
  jwt:
    secret: change-me-super-secret-key-please
    expiration-ms: 3600000   # 1 hora
```

> **Temporalmente sin migraciones** (si lo necesitas):  
> ```yaml
> spring:
>   flyway:
>     enabled: false
> ```

### 3) Dependencias clave del `pom.xml`
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `mysql-connector-j`
- `flyway-core` **+** `flyway-mysql`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

> Si ves en el arranque **“Unsupported Database: MySQL 8.0”**, falta la dependencia `flyway-mysql`.

---

## 🚀 Cómo ejecutar

### A) Desde fuente
```powershell
# Windows PowerShell
.\mvnw clean package -DskipTests
.\mvnw spring-boot:run
```

### B) JAR ejecutable
```powershell
.\mvnw clean package -DskipTests
java -jar target/Foro-HUB-0.0.1-SNAPSHOT.jar
```

La API quedará en: `http://localhost:8080`

---

## 🔐 Autenticación JWT (flujo)

1) **Registro** (opcional si ya tienes usuario):
   - **POST** `/auth/register`
   - Body:
     ```json
     { "username": "pepe", "password": "123456" }
     ```

2) **Login** → obtén token:
   - **POST** `/auth/login`
   - Body:
     ```json
     { "username": "pepe", "password": "123456" }
     ```
   - Respuesta:
     ```json
     { "type": "Bearer", "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..." }
     ```

3) **Usa el token** en endpoints protegidos:
   - Header: `Authorization: Bearer <TOKEN>`

---

## 📚 Endpoints

### Auth
- **POST** `/auth/register` – público  
  Crea un usuario. Retorna 200 con mensaje (o “ya existe”).

- **POST** `/auth/login` – público  
  Devuelve `{ "type": "Bearer", "token": "..." }`.

### Topics (protegidos con JWT)
- **GET** `/topics?page=0&size=20&sort=createdAt,desc`  
  Lista paginada.

- **GET** `/topics/{id}`  
  Detalle por id.

- **POST** `/topics`  
  Crea tópico. Body:
  ```json
  { "title": "Mi primer tópico", "content": "Hola foro 👋" }
  ```

- **PUT** `/topics/{id}`  
  Actualiza tópico. Body:
  ```json
  { "title": "Título editado", "content": "Contenido actualizado" }
  ```

- **DELETE** `/topics/{id}`  
  Borra tópico. Retorna **204 No Content**.

> **Reglas** (sugeridas/implementadas): solo el autor del tópico o un usuario con rol privilegiado puede actualizar/eliminar.

---

## 🧪 Uso rápido con Insomnia

1. **POST** `/auth/register` → crea usuario.
2. **POST** `/auth/login` → copia `token`.
3. En tus requests de `/topics`, **Auth → Bearer Token** → pega el token.
4. **POST** `/topics` con el JSON de creación.
5. **GET** `/topics` para listar.
6. **PUT/DELETE** para actualizar o eliminar por `id`.

> Puedes importar la **colección de Insomnia** incluida en este repo (o descargarla desde aquí si no está en el repo).

- Colección lista para importar: `Foro-HUB-Insomnia.json`  
  Contiene: Register, Login y CRUD de Topics con variables de entorno.

---

## 🗄️ Esquema base (Flyway `V1__init.sql`)

```sql
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
  enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS topics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL
);
```

> Si usas **`ddl-auto=validate`**, asegúrate de que las migraciones creen exactamente lo que esperan tus entidades.

---

## 🩺 Troubleshooting

- **401 Unauthorized**: token ausente/expirado; haz login otra vez.
- **403 Forbidden**: no tienes permisos (p. ej., no eres autor del tópico).
- **415 Unsupported Media Type**: faltó el `Content-Type: application/json`.
- **Flyway: Unsupported Database MySQL 8.0**: agrega `flyway-mysql` al `pom.xml`.
- **`release version 24 not supported`**: el POM pide Java 24 pero Maven usa otro JDK. Simplifica a **Java 21** en `<java.version>` y configura JDK 21 en el IDE/`JAVA_HOME`.

---

## 📝 Scripts cURL (alternativa a Insomnia)

```bash
# Login
curl -s -X POST http://localhost:8080/auth/login   -H "Content-Type: application/json"   -d '{"username":"pepe","password":"123456"}'

# Crear tópico (reemplaza TOKEN)
curl -s -X POST http://localhost:8080/topics   -H "Authorization: Bearer TOKEN"   -H "Content-Type: application/json"   -d '{"title":"Hola","content":"Primer post"}'

# Listar
curl -s -H "Authorization: Bearer TOKEN" http://localhost:8080/topics
```

---

## 📄 Licencia
Uso educativo.

## Guía de uso

Esta guía te lleva **paso a paso** desde la configuración local hasta las pruebas del API con **Insomnia** usando **JWT Bearer**.


https://github.com/user-attachments/assets/f51303c2-ad47-4841-89c2-7666f0c2c6d4


---

## 1) Requisitos

- **Java 21** (JDK)
- **MySQL 8.0** en local
- **Maven Wrapper** incluido en el repo (`mvnw` / `mvnw.cmd`)
- Cliente HTTP (recomendado: **Insomnia**)

---

## 2) Base de datos

1. Crea la base de datos:
   ```sql
   CREATE DATABASE foro_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. (Opcional) Usuario propio en MySQL:
   ```sql
   CREATE USER 'foro'@'localhost' IDENTIFIED BY 'foropass';
   GRANT ALL PRIVILEGES ON foro_hub.* TO 'foro'@'localhost';
   FLUSH PRIVILEGES;
   ```

---

## 3) Configuración de la app

Edita `src/main/resources/application.yml` con tus credenciales y un **secreto** largo para JWT:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/foro_hub?useSSL=false&serverTimezone=UTC
    username: root          # o 'foro'
    password: your_password # o 'foropass'
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true

app:
  jwt:
    secret: change-me-super-secret-key-please
    expiration-ms: 3600000  # 1 hora
```

> **Nota:** si necesitas levantar rápido sin migraciones, usa `spring.flyway.enabled=false` temporalmente.

---

## 4) Arranque

En **PowerShell** (Windows) desde la carpeta del proyecto:

```powershell
.\mvnw clean package -DskipTests
.\mvnw spring-boot:run
```
La API quedará en: **http://localhost:8080**

---

## 5) Flujo de autenticación (JWT)

1) **Registro** (opcional si ya tienes usuario):
   - **POST** `/auth/register`
   - Body JSON:
     ```json
     { "username": "pepe", "password": "123456" }
     ```

2) **Login** → obtener token:
   - **POST** `/auth/login`
   - Body JSON:
     ```json
     { "username": "pepe", "password": "123456" }
     ```
   - Respuesta:
     ```json
     { "type": "Bearer", "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..." }
     ```

3) **Usa el token** en las llamadas protegidas:
   - Header: `Authorization: Bearer <TOKEN>`

---

## 6) Pruebas con Insomnia (paso a paso)

### A) Variables de entorno (recomendado)
- Crea un **Environment** con:
  ```json
  {
    "baseUrl": "http://localhost:8080",
    "token": ""
  }
  ```

### B) Requests

1. **Register**
   - **POST** `{{ baseUrl }}/auth/register`
   - Body → JSON:
     ```json
     { "username": "pepe", "password": "123456" }
     ```

2. **Login**
   - **POST** `{{ baseUrl }}/auth/login`
   - Body → JSON:
     ```json
     { "username": "pepe", "password": "123456" }
     ```
   - Copia el `token` de la respuesta y pega su valor en `Environment.token`.

3. **Crear tópico**
   - **POST** `{{ baseUrl }}/topics`
   - **Auth** → *Bearer Token* → `{{ token }}`
   - **Headers** → `Content-Type: application/json`
   - **Body** → JSON:
     ```json
     { "title": "Mi primer tópico", "content": "Hola foro 👋" }
     ```
   - Esperado: `201 Created` + JSON con `id`.

4. **Listar tópicos**
   - **GET** `{{ baseUrl }}/topics?page=0&size=20&sort=createdAt,desc`
   - **Auth** → Bearer `{{ token }}`
   - Esperado: `200 OK` con paginación y tu tópico en `content`.

5. **Detalle**
   - **GET** `{{ baseUrl }}/topics/{id}` con Bearer `{{ token }}`

6. **Actualizar**
   - **PUT** `{{ baseUrl }}/topics/{id}` con Bearer `{{ token }}`
   - Body JSON:
     ```json
     { "title": "Título editado", "content": "Contenido actualizado" }
     ```

7. **Eliminar**
   - **DELETE** `{{ baseUrl }}/topics/{id}` con Bearer `{{ token }}`
   - Esperado: `204 No Content`

> **Nota de permisos:** por defecto, el **autor** del tópico (o un usuario con rol elevado, si lo configuraste) puede editar/eliminar.

---

## 7) cURL (alternativa rápida)

```bash
# Login
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"pepe","password":"123456"}'

# Crear tópico (reemplaza TOKEN)
curl -s -X POST http://localhost:8080/topics \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Hola","content":"Primer post"}'

# Listar
curl -s -H "Authorization: Bearer TOKEN" http://localhost:8080/topics
```

---

## 8) Problemas frecuentes

- **401 Unauthorized**: token ausente o expirado → vuelve a `/auth/login` y renueva.
- **403 Forbidden**: no tienes permisos (p. ej., no eres el autor del tópico).
- **415 Unsupported Media Type**: faltó `Content-Type: application/json`.
- **Flyway – Unsupported Database: MySQL 8.0**: agrega la dependencia `flyway-mysql` en `pom.xml`.
- **Compilación – `release version 24 not supported`**: usa **Java 21** en `<java.version>` y verifica que Maven/IDE usen JDK 21.

---

## 9) Producción (pistas rápidas)

- Usa variables de entorno para credenciales y secreto JWT:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `APP_JWT_SECRET` (si parametrizas con `${JWT_SECRET:...}` en `application.yml`)
- Cambia el puerto con `SERVER_PORT` o `server.port`.
- Revisa tiempos de expiración del token (`app.jwt.expiration-ms`).
- Configura CORS si tu frontend está en otro dominio.

---

¡Listo! Con esto puedes levantar, autenticarte y recorrer todo el CRUD del foro con Insomnia.
