# SPE Calculator CI/CD

End-to-end CI/CD pipeline for a Calculator REST API
(CSE 816 - Software Production Engineering mini-project).

Stack: Java 17, Spring Boot 4.1, Maven, JUnit 5, Git/GitHub, Jenkins, Docker, Docker Hub, Docker Compose, Nginx.

## Quick start

    ./mvnw clean test
    SERVER_PORT=9090 ./mvnw spring-boot:run
    curl "http://localhost:9090/api/v1/add?a=2&b=3"

The full documentation is added as the project develops.

## CI/CD

Every push to `main` triggers the Jenkins pipeline:
checkout, build, test, Docker build, push to Docker Hub, deploy with Docker Compose behind Nginx.
