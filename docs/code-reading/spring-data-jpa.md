# Lectura de código: Spring Data JPA

## Qué leí

- Spring Data JPA (repositorios)
- Métodos como `findById`, `findAll`
- Query derivation (ej: `findByUsernameContaining`)
- Uso de interfaces `JpaRepository`

---

## Qué entendí

Spring Data JPA permite crear consultas automáticamente a partir del nombre de los métodos.

Ejemplo:
- `findByUsernameContaining` genera una consulta SQL con LIKE
- `findById` usa internamente el EntityManager

También entendí que:
- No es necesario escribir SQL manual para consultas simples
- El framework genera las queries dinámicamente
- Se basa en proxies que implementan las interfaces de repositorio

---

## Qué no entendí

- Cómo funciona internamente la generación de proxies
- Cómo Spring transforma exactamente los nombres de métodos en queries SQL
- Detalles internos del EntityManager

---

## Qué puedo aplicar en codexrm-server

- Seguir usando query derivation para consultas simples
- Evitar escribir consultas manuales innecesarias
- Mantener nombres de métodos claros y descriptivos
- Aprovechar mejor `JpaRepository`

---

## Qué decisiones de diseño tomaron y si estoy de acuerdo

Spring Data JPA prioriza:
- Simplicidad
- Reducción de código repetitivo
- Abstracción de la base de datos

Estoy de acuerdo con este enfoque porque:
- Hace el código más limpio
- Reduce errores
- Acelera el desarrollo

Sin embargo, en casos complejos puede ser necesario usar queries personalizadas.