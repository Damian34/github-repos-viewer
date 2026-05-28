# GitHub Repository Viewer

A Spring Boot application for browsing GitHub user repositories. Returns only non-fork repositories with branch names and latest commit SHA.

## Technologies
- Java 25
- Kotlin
- Spring Boot 4.0
- Gradle

## Endpoints

- `GET /api/github/repositories/{username}` - get a list of non-fork repositories for the specified GitHub user
