# Codelabs Spring Boot — Desarrollo de Software III

Repositorio académico del microservicio **`cliente-service`**, desarrollado como parte de los laboratorios de servicios REST con Spring Boot. Los ejercicios se construyen progresivamente: desde el primer CRUD hasta la organización de la arquitectura y el manejo de errores.

**Estudiante:** Luz Amelia Ibargüen Ibarra  
**Programa:** Tecnología en Desarrollo de Software — Universidad del Valle, sede Tuluá  
**Asignatura:** Desarrollo de Software III

## Codelabs y ramas

Cada enlace permite revisar la versión correspondiente al avance de ese laboratorio.

| Codelab | Tema | Rama |
| --- | --- | --- |
| 01 | Primer microservicio REST y operaciones CRUD | [`codelab-01`](https://github.com/EmpressQueenGalaxy/codelab/tree/codelab-01) |
| 02 | Configuración de la aplicación, conexiones y Actuator | [`codelab-02`](https://github.com/EmpressQueenGalaxy/codelab/tree/codelab-02) |
| 03 | Paginación, transacciones y validaciones | [`codelab-03`](https://github.com/EmpressQueenGalaxy/codelab/tree/codelab-03) |
| 04 | Arquitectura limpia y manejo global de excepciones | [`codelab-04`](https://github.com/EmpressQueenGalaxy/codelab/tree/codelab-04) |

La rama **`main`** contiene la versión más reciente integrada, correspondiente al avance del codelab 04.

## Tecnologías utilizadas

- Java 21 y Maven
- Spring Boot y Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL 16 y Docker Compose
- Lombok, Bean Validation y Spring Boot Actuator
- IntelliJ IDEA y Git/GitHub

## Estructura y funcionamiento

El servicio ofrece una API REST para administrar clientes y consultar regiones. Las peticiones HTTP se reciben en el controlador, se procesan mediante la lógica de aplicación y acceden a PostgreSQL a través de la persistencia con JPA.

## Ejecución local

**Requisitos:** Java 21 y Docker con Compose.

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/EmpressQueenGalaxy/codelab.git
   cd codelab
   ```

2. Configurar la variable de entorno `DB_PASSWORD` con una contraseña local para PostgreSQL. En PowerShell:

   ```powershell
   $env:DB_PASSWORD = "tu_clave_local"
   ```

   En Bash (Linux/macOS):

   ```bash
   export DB_PASSWORD="tu_clave_local"
   ```

   Usa la **misma contraseña** al arrancar Docker y Spring Boot. No publiques contraseñas reales en el repositorio. Si ya existe el volumen de PostgreSQL con otra contraseña, cambiar esta variable no modifica automáticamente la contraseña del usuario existente.

3. Iniciar PostgreSQL:

   ```bash
   docker compose up -d
   ```

4. Iniciar Spring Boot en otra terminal, conservando la variable `DB_PASSWORD`:

   **Windows (PowerShell):**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **Linux/macOS:**

   ```bash
   ./mvnw spring-boot:run
   ```

5. Probar el servicio en `http://localhost:8080`. El archivo [`requests.http`](requests.http) contiene ejemplos de solicitudes para ejecutar desde un cliente HTTP compatible (por ejemplo, IntelliJ IDEA).

> **Nota:** La configuración actual usa `spring.jpa.hibernate.ddl-auto=create-drop`, que recrea el esquema y elimina las tablas al cerrar la aplicación. Está pensada para prácticas, no para conservar datos de producción.

## Endpoints de referencia

Prefijo de rutas: `/api/v1/cliente-service`

| Método | Ruta | Propósito |
| --- | --- | --- |
| `GET` | `/clientes/regiones` | Consultar regiones |
| `GET` | `/clientes` | Consultar clientes |
| `GET` | `/clientes/{id}` | Consultar cliente por ID |
| `POST` | `/clientes` | Registrar cliente |
| `PUT` | `/clientes/{id}` | Actualizar cliente |
| `DELETE` | `/clientes` | Eliminar cliente según los datos de la solicitud |

Los ejemplos de cuerpos JSON y pruebas manuales están en [`requests.http`](requests.http). La disponibilidad y el comportamiento exacto de cada operación dependen de la implementación de la rama consultada.

## Revisión y entrega

Para revisar el progreso, seleccionar el enlace de cada rama en la tabla **Codelabs y ramas**. Este repositorio es público, por lo que el docente puede acceder al código y al historial de cambios.

**Repositorio:** https://github.com/EmpressQueenGalaxy/codelab
