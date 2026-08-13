# FirstClub Membership Service

This is the Spring Boot backend for the FirstClub Membership Program.

## Technology baseline

- Java 21 (LTS)
- Maven 3.9.x
- Spring Boot 4.0.7
- Spring MVC, Bean Validation, and Actuator

The database, Flyway migrations, and membership features will be added in later milestones. Starting with a small runnable application makes each new Spring concept easier to understand and verify.

## Project structure

```text
src/main/java/com/firstclub/membership/
  MembershipServiceApplication.java  Application entry point
  common/controller/PingController.java  Temporary API smoke test

src/main/resources/
  application.yml  Application configuration
```

## Run locally

After Java 21 and Maven are installed and available in a new terminal:

```powershell
mvn spring-boot:run
```

Then open the URL:

- `http://localhost:8080/api/v1/health` returns `{ "status": "ok" }`.

## What each dependency does

- `spring-boot-starter-webmvc`: REST controllers and embedded Tomcat.
- `spring-boot-starter-validation`: validates request input later using annotations such as `@NotNull`.
- `spring-boot-starter-actuator`: health endpoints and operational checks.
- `spring-boot-starter-test`: automated tests.
