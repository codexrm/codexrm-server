# CodexRM Server

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

Back-end del sistema **CodexRM**, encargado de gestionar la comunicación entre los clientes (escritorio, web y móvil) y la base de datos.

El servidor está desarrollado utilizando **Spring Boot**, framework que facilita el desarrollo y despliegue de **servicios REST**.

El sistema implementa el patrón de sincronización **Last Write Wins (La última escritura gana)** para resolver conflictos en la actualización de datos.

La autenticación se realiza mediante **JWT (JSON Web Token)**, permitiendo un intercambio seguro de información entre clientes y servidor.

---

# Funcionalidades

## Gestión de referencias

El servidor permite:

* Obtener el listado de todas las referencias.
* Obtener el listado de referencias de un usuario.
* Obtener el listado de usuarios registrados.

Las consultas permiten:

* **Paginación**
* **Ordenamiento**
* **Filtrado por criterios**

## Sincronización de datos

El sistema permite sincronizar referencias entre múltiples clientes utilizando el patrón:

**Lectura / Escritura de datos con resolución "Last Write Wins"**

Esto permite mantener la consistencia de datos entre diferentes dispositivos.

## Importación y exportación

Las referencias pueden:

* **Exportarse** en formato **RIS** o **BibTeX**
* **Importarse** desde archivos **RIS** o **BibTeX**

---

# Tecnologías utilizadas

* Java 21
* Spring Boot
* PostgreSQL
* JWT Authentication
* Maven
* Docker
* Docker Compose

---

# Ejecución con Docker

El proyecto puede ejecutarse completamente utilizando **Docker Compose**.

## Requisitos

* Docker
* Docker Compose

---

# Construcción del proyecto

Primero se debe generar el archivo ejecutable `.jar`.

```bash
mvn clean package
```

Esto generará el archivo:

```
target/server-0.0.1-SNAPSHOT.jar
```

---

# Ejecutar el sistema

Para iniciar el servidor y la base de datos:

```bash
docker-compose up --build
```

Servicios iniciados:

| Servicio       | Descripción              | Puerto |
| -------------- | ------------------------ |--------|
| codexrm-server | API REST Spring Boot     | 8080   |
| codexrm-db     | Base de datos PostgreSQL | 5433   |

---

# Variables de entorno

El servidor utiliza las siguientes variables de entorno:

| Variable                   | Descripción                  | Valor por defecto                 |
| -------------------------- | ---------------------------- |-----------------------------------|
| SPRING_PROFILES_ACTIVE     | Perfil activo de Spring      | prod                              |
| SPRING_DATASOURCE_URL      | URL de conexión a PostgreSQL | jdbc:postgresql://db:5433/codexrm |
| SPRING_DATASOURCE_USERNAME | Usuario de base de datos     | codexrm                           |
| SPRING_DATASOURCE_PASSWORD | Contraseña de base de datos  | codexrm                           |

---

# Endpoints principales

Ejemplos de endpoints disponibles:

| Método | Endpoint              | Descripción                   |
| ------ | --------------------- | ----------------------------- |
| GET    | /references           | Obtener referencias           |
| GET    | /references/user/{id} | Referencias de un usuario     |
| GET    | /users                | Listado de usuarios           |
| POST   | /sync                 | Sincronización de referencias |
| POST   | /import               | Importar referencias          |
| GET    | /export               | Exportar referencias          |

---

# Health Check

Para verificar que el servidor está funcionando:

```
http://localhost:8080/actuator/health
```

---

# Detener los contenedores

Para detener el sistema:

```bash
docker-compose down
```

---

# Estructura del proyecto

```
server/
│
├── src/
│   ├── main/
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Autor

Proyecto desarrollado como parte del sistema **CodexRM**.
