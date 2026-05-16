# ModelMapper - Inheritance & Polymorphic Mapping Analysis

## 1. Overview

ModelMapper is a Java library used to automatically map between objects (typically Entities ↔ DTOs). One of its more complex behaviors is how it handles **inheritance hierarchies** and **polymorphic types**.

This document summarizes how ModelMapper behaves in inheritance scenarios and how it compares with manual mapping strategies used in our `DTOConverter` design.

---

## 2. Inheritance Mapping Behavior

### 2.1 Basic Inheritance Support

ModelMapper supports inheritance by default through property introspection.

Given:

```java
class Reference {
    String title;
}

class ArticleReference extends Reference {
    String author;
}
```

And DTOs:

```java
class ReferenceDTO {
    String title;
}

class ArticleReferenceDTO extends ReferenceDTO {
    String author;
}
```

ModelMapper can map correctly when the target type explicitly matches the source subtype:

```java
ArticleReference reference = new ArticleReference();
reference.setTitle("Deep Learning");
reference.setAuthor("Guerra,Luis");

ArticleReferenceDTO dto = modelMapper.map(reference, ArticleReferenceDTO.class);
```

Result:

- title is mapped (inherited field)
- author is mapped (subclass field)

---

### 2.2 Inheritance Limitation with Polymorphism

Problems appear when using polymorphic references.

Example:

```java
Reference reference = new ArticleReference();
```

Mapping:

```java
ReferenceDTO dto = modelMapper.map(reference, ReferenceDTO.class);
```

Behavior:

- ModelMapper only considers the declared type (`Reference`)
- It does NOT detect the runtime type (`ArticleReference`)
- Subclass-specific fields like `author` are ignored

---

## 3. Key Insight

ModelMapper relies on static type information, not runtime polymorphism.

This means:

- Works well when source and target types are explicit
- Fails when using base class references

---

## 4. Configuration Options

ModelMapper provides configuration options that can improve inheritance mapping behavior, although they do not fully solve polymorphic limitations.

Example:

```java
modelMapper.getConfiguration()
    .setFieldMatchingEnabled(true)
    .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
```

Useful configuration options:

- `setFieldMatchingEnabled(true)`
    - Enables direct field matching

- `setFieldAccessLevel(PRIVATE)`
    - Allows mapping private fields

- `setAmbiguityIgnored(true)`
    - Avoids errors when multiple fields match

These options improve flexibility, but runtime subtype resolution still requires manual handling.

---

## 5. DTO Conversion Strategies

### Using ModelMapper

ModelMapper works well for:

- simple DTOs
- flat object structures
- low-maintenance mappings

Advantages:

- less boilerplate
- faster implementation
- cleaner service code

Limitations:

- weaker control over polymorphism
- hidden mapping behavior
- harder debugging in inheritance-heavy domains

---

### Using Manual DTOConverter

Manual mapping provides explicit control over conversion behavior.

Advantages:

- predictable mappings
- better subtype handling
- easier debugging
- clearer business logic

Limitations:

- more verbose code
- higher maintenance effort

---

## 6. Conclusion

Inheritance mapping works correctly in simple cases, but polymorphic mapping is limited.

This becomes important when designing DTO layers in systems with inheritance-heavy domain models.

As a result, relying solely on ModelMapper can introduce hidden mapping issues in complex object hierarchies.

For the current architecture, DTOConverter-style manual mapping provides better control and predictability for polymorphic scenarios.