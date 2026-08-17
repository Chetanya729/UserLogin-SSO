# SSO Project

A small Spring Boot playground for exploring authentication in Spring Security 6 — form login backed by an in-memory user store, plus an optional "Sign in with Google" flow via OAuth2 / OIDC. Built while learning; not intended for production use.

## Tech stack

- Spring Boot 4.1.0
- Spring Security 6 (form login + OAuth2 client)
- Thymeleaf + `thymeleaf-extras-springsecurity6`
- JJWT 0.12 (JWT scaffolding, not yet wired into the auth flow)
- Lombok
- Java 26, Maven

## Features

- Form-based login against an in-memory user map (passwords BCrypt-hashed at startup)
- OAuth2 login with Google as an identity provider (coexists with form login)
- Protected `/home` page that reads the current principal via Thymeleaf Security dialect
- Session logout with redirect back to `/login?logout`

## Project layout

```
src/main/java/com/example/SSO_project/
├── Config/SecurityConfig.java          # Security filter chain, encoders
├── Controller/UserController.java      # login / home / logout view mappings
├── Service/                            # UserDetailService, JwtTokenService
├── ServiceImpl/                        # In-memory user store, JWT generator
├── domain/User.java                    # Lombok POJO
└── SsoProjectApplication.java
src/main/resources/
├── application.yml
└── templates/{login,home}.html
```

## Prerequisites

- JDK 26
- Maven (or use the bundled `./mvnw` wrapper)
- A Google OAuth 2.0 Client (Web application type) if you want to try the Google login

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  security:
    jwt:
      secret-key: <base64-encoded 32+ byte secret>
      expiration: 3600000
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email
```

Export the Google credentials before running:

```bash
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret
```

In Google Cloud Console, add this authorized redirect URI to your OAuth client:

```
http://localhost:8080/login/oauth2/code/google
```

## Run

```bash
./mvnw spring-boot:run
```

The app starts on http://localhost:8080. Open `/login`.

## Test credentials

Seed users are defined in `UserDetailServiceImpl` (hashed at startup):

| Username | Password |
|----------|----------|
| chetanya | chetanya123 |
| rahul    | rahul123 |
| kumar    | kumar123 |
| dog      | dog123 |

Or click **Sign in with Google** to authenticate through your Google account.

## Known limitations

- In-memory user store — resets on every restart.
- CSRF is disabled for simplicity; re-enable before using anywhere real.
- The `JwtTokenService` is present but unused — form login uses session cookies, not JWTs.
- No user provisioning for OAuth logins: Google users authenticate but aren't persisted anywhere.
