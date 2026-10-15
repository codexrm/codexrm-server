# Code reading: Spring Data JPA

## What I read

- Spring Data JPA (repositories)
- Methods such as `findById`, `findAll`
- Query derivation (e.g. `findByUsernameContaining`)
- Use of `JpaRepository` interfaces

---

## What I understood

Spring Data JPA makes it possible to create queries automatically from
method names.

Example:
- `findByUsernameContaining` generates a SQL query with LIKE
- `findById` uses the EntityManager internally

I also understood that:
- It is not necessary to write manual SQL for simple queries
- The framework generates the queries dynamically
- It relies on proxies that implement the repository interfaces

---

## What I did not understand

- How proxy generation works internally
- How exactly Spring transforms method names into SQL queries
- Internal details of the EntityManager

---

## What I can apply in codexrm-server

- Keep using query derivation for simple queries
- Avoid writing unnecessary manual queries
- Keep method names clear and descriptive
- Make better use of `JpaRepository`

---

## What design decisions they made and whether I agree

Spring Data JPA prioritizes:
- Simplicity
- Reduction of repetitive code
- Database abstraction

I agree with this approach because:
- It makes the code cleaner
- It reduces errors
- It speeds up development

However, in complex cases it may be necessary to use custom queries.