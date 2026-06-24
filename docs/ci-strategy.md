# CI Strategy for Multi-Repository Maven Architecture

## Current Situation

The server repository depends on artifacts that are not available during GitHub Actions execution:

- Parent POM: io.github.codexrm:codexrm:1.0.0
- EILibrary
- jRis

Currently these dependencies are resolved only in the local development workspace.

---

## Evaluated Options

### Option 1: Publish Internal Libraries

Publish:

- EILibrary
- jRis

as Maven artifacts.

Advantages
- Standard Maven workflow
- Reusable by multiple repositories
- CI friendly

Disadvantages
- Requires artifact version management
- Requires package hosting

---

### Option 2: Publish Parent POM

Publish the parent POM as an artifact.

Advantages
- Keeps current Maven structure
- Minimal project changes

Disadvantages
- Additional artifact to maintain

---

### Option 3: GitHub Packages

Use GitHub Packages as the Maven registry.

Advantages
- Native GitHub integration
- Compatible with GitHub Actions
- Supports private packages

Disadvantages
- Requires authentication tokens
- Package publishing workflow required

---

#### Option 4: Dedicated CI Repository

Create a repository containing:

- parent pom
- server
- EILibrary
- jRis

for CI execution.

Advantages
- No artifact publishing required

Disadvantages
- Repository duplication
- Maintenance overhead

---

### Option 5: Monorepo

Move all modules into a single Maven multi-module repository.

Advantages
- Simplified dependency management
- Simplified CI

Disadvantages
- Major repository restructuring

---

### Option 6: Remove Parent Inheritance

Make each repository self-contained.

Advantages
- Independent repositories

Disadvantages
- Configuration duplication
- Harder maintenance

---

## Recommended Strategy

The preferred solution is to use GitHub Packages as the artifact repository.

Under this approach:

1. Publish the parent POM as a Maven artifact.
2. Publish EILibrary as a versioned Maven artifact.
3. Publish jRis as a versioned Maven artifact.
4. Configure GitHub Actions to authenticate against GitHub Packages.
5. Resolve all internal dependencies through Maven during CI execution.

This approach preserves the current multi-repository architecture while enabling reliable CI execution.

---

## Future CI Implementation Path
1. Publish parent POM.
2. Publish EILibrary.
3. Publish jRis.
4. Update Maven settings.xml in CI.
5. Enable full build and integration testing in GitHub Actions.

---

## Decision

GitHub Packages was selected as the preferred strategy because it:

- Supports Maven artifact publishing and consumption
- Integrates natively with GitHub Actions
- Preserves the current multi-repository architecture
- Avoids repository duplication
- Requires minimal structural changes
