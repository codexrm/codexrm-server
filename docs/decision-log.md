# JPA Inheritance Strategy Decision

## Context

The system models different types of references such as Article, Book, Thesis, ConferencePaper, etc.

Currently, JPA inheritance is configured using:

@Inheritance(strategy = InheritanceType.JOINED)

This means the base entity (Reference) is stored in one table, and each subtype has its own table linked by a foreign key.

---

## JOINED Strategy

### Description

Each class in the inheritance hierarchy is mapped to its own table.

Queries involving subclasses require JOIN operations between the base table and the subclass tables.

---

## Pros

- Clear and normalized database structure
- Avoids nullable columns for subtype-specific fields
- Good separation of concerns between entities
- Easier to maintain and extend

---

## Cons

- Requires JOIN operations in queries
- Potential performance impact in complex queries
- Pagination and large queries may be slightly slower

---

## Alternatives

### SINGLE_TABLE

**Description:**
All entities are stored in a single table with a discriminator column.

**Pros:**
- No JOINs required
- Better read performance

**Cons:**
- Many nullable columns
- Poor data normalization
- Harder to maintain as the model grows

---

### TABLE_PER_CLASS

**Description:**
Each class has its own independent table.

**Pros:**
- No JOINs required

**Cons:**
- Data duplication
- Complex queries when accessing the base type
- Not ideal for polymorphic queries

---

## Performance Considerations

- JOINED introduces additional JOINs when retrieving subclass data
- For this system, queries are not highly complex or performance-critical
- The trade-off between performance and data integrity favors JOINED

---

## Decision

We decided to keep the JOINED inheritance strategy.

---

## Justification

The system manages multiple distinct reference types with different fields.

Using JOINED ensures:
- Clean database design
- Better scalability when adding new reference types
- Maintainable and readable data model

Performance impact is acceptable given the current system requirements.

---

# Transactional Boundaries and Isolation Levels

## Context

The system currently has limited usage of @Transactional annotations.

A review of the codebase shows that @Transactional is only used in a few service classes (e.g., RefreshTokenService and UserDetailsServiceImpl), while most operations rely on default Spring Data JPA behavior.

---

## Current State

- @Transactional is not consistently applied across service methods
- Most CRUD operations rely on implicit transaction handling by Spring Data JPA
- No clearly defined transactional strategy is present

---

## PostgreSQL Isolation Level

PostgreSQL uses the default isolation level:

READ COMMITTED

### Characteristics:

- Prevents dirty reads
- Allows non-repeatable reads
- Each query sees only committed data

---

## Analysis

### Transactional usage

Transactions are important when:
- Multiple database operations must succeed or fail together
- Data consistency is critical
- Business logic spans multiple repository calls

### Current system evaluation

- Most operations are simple (single insert/update)
- No complex multi-step transactional flows were identified
- Current behavior is sufficient for existing use cases

---

## Potential Improvements

- Apply @Transactional at the service layer for write operations
- Ensure that future complex operations (e.g., batch processing or synchronization endpoints) are transactional
- Avoid placing @Transactional in controllers (best practice)

---

## Decision

Keep the current transactional setup, but recommend improving consistency by introducing @Transactional at the service layer where appropriate.

---

## Justification

The system does not currently require advanced transaction management.

PostgreSQL's default isolation level (READ COMMITTED) provides sufficient consistency for current operations.

Introducing transactions selectively improves reliability without adding unnecessary complexity.