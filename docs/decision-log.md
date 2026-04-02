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