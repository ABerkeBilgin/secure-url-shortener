# 🚀 Secure URL Shortener & Infrastructure

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green) ![Docker](https://img.shields.io/badge/Docker-Compose-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791) ![Redis](https://img.shields.io/badge/Redis-Caching-red) ![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2-purple) ![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-e6522c) ![Grafana](https://img.shields.io/badge/Grafana-Dashboard-F46800) [![CI/CD](https://github.com/ABerkeBilgin/secure-url-shortener/actions/workflows/maven.yml/badge.svg)](https://github.com/ABerkeBilgin/secure-url-shortener/actions/workflows/maven.yml)

A high-performance, secure, and observable URL Shortener API built with modern backend architecture practices. This project demonstrates a production-ready infrastructure using Docker, OAuth2 security, and comprehensive monitoring.

## 🏗 Architecture

* **Core:** Spring Boot 3 (Java 21)
* **Database:** PostgreSQL (Persistent Storage)
* **Cache:** Redis (High-speed redirection)
* **Security:** Keycloak (OAuth2 / OIDC Resource Server)
* **Monitoring:** Prometheus (Metrics Collection) & Grafana (Visualization)
* **DevOps:** Docker Compose & GitHub Actions (CI/CD)
* **Testing:** Testcontainers (Integration Tests with real DB/Cache)

## 🚀 Getting Started

### Prerequisites
* Docker & Docker Compose

### Installation & Running
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ABerkeBilgin/secure-url-shortener.git
    cd secure-url-shortener
    ```

2.  **Start the infrastructure:**
    ```bash
    docker-compose up -d --build
    ```
    *Wait for about 30-60 seconds for Keycloak and Database to initialize.*

## 🔌 Endpoints & Usage

| Service | URL | Credentials (If applicable) |
| :--- | :--- | :--- |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Auth via Token |
| **Keycloak** | `http://localhost:8081` | `admin` / `admin` |
| **Grafana** | `http://localhost:3000` | `admin` / `admin` |
| **Prometheus** | `http://localhost:9090` | - |

### 🔑 How to Authenticate (Swagger)
1.  Go to Keycloak (`http://localhost:8081`) -> Console.
2.  Log in with `admin` / `admin`.
3.  Get an Access Token via Curl (Terminal):
    ```bash
    curl -X POST http://localhost:8081/realms/shortener-realm/protocol/openid-connect/token \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "client_id=url-shortener-client" \
      -d "client_secret=not-required-public-client" \
      -d "username=user" \
      -d "password=password" \
      -d "grant_type=password"
    ```
    *(Note: Use the test user credentials if you created one, or create a new user in Keycloak UI).*

4.  Copy the `access_token` and use it in Swagger **Authorize** button as: `Bearer <YOUR_TOKEN>`.

## 🧪 Testing

The project uses **Testcontainers** for integration testing to ensure reliability.

```bash
./mvnw verify
```