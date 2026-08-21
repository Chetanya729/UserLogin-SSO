# SSO Project

A Spring Boot playground for exploring authentication with Spring Security 6 — form-based login with a MySQL-backed user store, OAuth2 / OIDC social login (Google, GitHub, Facebook), password reset via email, role-based access control, and remember-me support. Built while learning; not intended for production use.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 26 |
| Framework | Spring Boot 4.1.0, Spring Security 6 |
| Build / Package Manager | Maven (wrapper included: `./mvnw`) |
| Persistence | Spring Data JPA, MySQL 8 |
| Auth | Form login, OAuth2 Client (Google, GitHub, Facebook), JJWT 0.12.6 |
| Templating | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Mail | Spring Boot Mail (SMTP via [Resend](https://resend.com)) |
| Validation | Spring Boot Starter Validation |
| Other | Lombok |
| Containerisation | Docker (multi-stage), Docker Compose |

## Features

- **Form-based login** with BCrypt-hashed passwords stored in MySQL
- **OAuth2 / OIDC social login** — Google, GitHub, and Facebook
- **User registration** with validation
- **Password reset** flow via email token
- **Role-based access control** (`USER` / `ADMIN` roles with dedicated views)
- **Remember-me** persistent token support
- **Profile page** for authenticated users
- Protected pages rendered with Thymeleaf Security dialect
- Session logout with redirect

## Requirements

- **JDK 26**
- **Maven 3.9+** (or use the bundled `./mvnw` wrapper)
- **MySQL 8** (local install or via Docker Compose)
- OAuth2 credentials for any social provider you want to enable (Google, GitHub, Facebook)

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | Yes (prod) | JDBC URL, e.g. `jdbc:mysql://localhost:3306/userDbSSOProject?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | Yes (prod) | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes (prod) | MySQL password |
| `DATABASE_USERNAME` | No | Dev-profile MySQL username (default: `root`) |
| `DATABASE_PASSWORD` | No | Dev-profile MySQL password (default: empty) |
| `GOOGLE_CLIENT_ID` | No | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | No | Google OAuth2 client secret |
| `GIT_CLIENT_ID` | No | GitHub OAuth2 client ID |
| `GIT_CLIENT_SECRET` | No | GitHub OAuth2 client secret |
| `FACEBOOK_CLIENT_ID` | No | Facebook OAuth2 client ID |
| `FACEBOOK_CLIENT_SECRET` | No | Facebook OAuth2 client secret |
| `RESEND_API_KEY` | No | API key for Resend SMTP (password-reset emails) |
| `APP_REMEMBER_KEY` | No | Secret key for remember-me tokens (default provided in dev profile) |
| `SPRING_PROFILES_ACTIVE` | No | Active Spring profile (default: `dev`) |
| `PORT` | No | Server port (default: `8080`) |

## Setup & Run

### Local (dev profile)

1. **Start MySQL** on `localhost:3306` (or adjust `application-dev.yml`).

2. **Export credentials** (at minimum the OAuth providers you want):

   ```bash
   export GOOGLE_CLIENT_ID=your-id
   export GOOGLE_CLIENT_SECRET=your-secret
   export GIT_CLIENT_ID=your-id
   export GIT_CLIENT_SECRET=your-secret
   export FACEBOOK_CLIENT_ID=your-id
   export FACEBOOK_CLIENT_SECRET=your-secret
   export RESEND_API_KEY=your-key
   ```

3. **Run the app:**

   ```bash
   ./mvnw spring-boot:run
   ```

   The app starts at **http://localhost:8080**. Open `/login` to begin.

### Docker Compose (MySQL + app)

1. Create a `.env` file next to `docker-compose.yml`:

   ```dotenv
   DB_ROOT_PASSWORD=rootpass
   GOOGLE_CLIENT_ID=...
   GOOGLE_CLIENT_SECRET=...
   GIT_CLIENT_ID=...
   GIT_CLIENT_SECRET=...
   FACEBOOK_CLIENT_ID=...
   FACEBOOK_CLIENT_SECRET=...
   ```

2. Start everything:

   ```bash
   docker compose up --build -d
   ```

   The app is available at **http://localhost:8080**.

### OAuth Redirect URIs

Register these redirect URIs with each provider's developer console:

| Provider | Redirect URI |
|----------|-------------|
| Google | `http://localhost:8080/login/oauth2/code/google` |
| GitHub | `http://localhost:8080/login/oauth2/code/github` |
| Facebook | `http://localhost:8080/login/oauth2/code/facebook` |

## Scripts / Maven Goals

```bash
./mvnw spring-boot:run          # Run the application
./mvnw clean package             # Build JAR (target/*.jar)
./mvnw clean package -DskipTests # Build JAR, skip tests
./mvnw test                      # Run tests
```

## Tests

Tests live under `src/test/java/`. Run them with:

```bash
./mvnw test
```

<!-- TODO: add integration / controller tests beyond the default context-load test -->

## Project Structure

```
SSO-project/
├── Dockerfile                          # Multi-stage build (JDK 26 → JRE 26)
├── docker-compose.yml                  # MySQL 8 + Spring Boot app
├── mvnw / mvnw.cmd                     # Maven wrapper
├── pom.xml                             # Maven build descriptor
└── src/
    ├── main/
    │   ├── java/com/example/SSO_project/
    │   │   ├── Config/
    │   │   │   ├── CustomPersistentTokenRepository.java
    │   │   │   ├── RoleBasedAuthenticationSuccessHandler.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── Controller/
    │   │   │   ├── AdminController.java
    │   │   │   ├── HomeController.java
    │   │   │   ├── PasswordResetController.java
    │   │   │   ├── ProfileController.java
    │   │   │   ├── RegisterController.java
    │   │   │   └── UserController.java
    │   │   ├── Exception/
    │   │   │   ├── InvalidTokenException.java
    │   │   │   ├── PasswordMismatchException.java
    │   │   │   └── UserAlreadyExists.java
    │   │   ├── Repository/
    │   │   │   ├── PasswordResetRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── Service/
    │   │   │   ├── CustomOAuth2UserService.java
    │   │   │   ├── CustomOidcUserService.java
    │   │   │   ├── JwtTokenService.java
    │   │   │   ├── PasswordResetService.java
    │   │   │   ├── UserDetailService.java
    │   │   │   └── UserRegisterationService.java
    │   │   ├── ServiceImpl/
    │   │   │   ├── JwtTokenServiceImpl.java
    │   │   │   ├── PasswordResetServiceImpl.java
    │   │   │   ├── UserDetailServiceImpl.java
    │   │   │   └── UserRegisterationServiceImpl.java
    │   │   ├── domain/
    │   │   │   ├── PROVIDER.java
    │   │   │   ├── PasswordResetToken.java
    │   │   │   ├── ROLE.java
    │   │   │   ├── RegisterRequest.java
    │   │   │   ├── User.java
    │   │   │   └── UserRegister.java
    │   │   └── SsoProjectApplication.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── static/css/style.css
    │       └── templates/
    │           ├── admin-home.html
    │           ├── error.html
    │           ├── forgot-password.html
    │           ├── home.html
    │           ├── landing.html
    │           ├── login.html
    │           ├── profile.html
    │           ├── register.html
    │           └── reset-password.html
    └── test/
        └── java/com/example/SSO_project/
            └── SsoProjectApplicationTests.java
```

## Known Limitations

- The `JwtTokenService` is scaffolded but not wired into the main auth flow (form login uses session cookies).
- CSRF is disabled in `SecurityConfig` for simplicity — re-enable before any real deployment.
- No rate-limiting on password-reset or login endpoints.
- <!-- TODO: add production-ready logging & monitoring configuration -->

## License

<!-- TODO: add license information -->
No license specified.
