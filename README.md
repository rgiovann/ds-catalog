# DS Catalog - Authentication with Keycloak and Spring Boot 3.x

This project is part of Nelio Alves' bootcamp and implements the **DS Catalog** system, a product catalog application. Originally, the project used **Spring Authorization 2.0** with the `grant_type=password` flow, which is no longer recommended for security reasons. As part of my learning process, I migrated the implementation to **Spring Boot 3.0** and, later, to the **OAuth 2.0 Authorization Code with PKCE** flow integrated with **Keycloak** as the Authorization Server.

This document describes the authentication flow implementation, the Keycloak configuration, and how to test the application, including authenticated calls to protected endpoints.

---

## Goal

The main goal was to understand the OAuth 2.0 Authorization Code with PKCE flow, integrating Keycloak with Spring Boot 3.x and Spring Security. The implementation was focused on learning, so some production-ready details were not addressed (e.g.: robust `state` validation, dynamic `code_verifier` generation).

---

## Project Structure

### Main Classes

- **`AuthController`**:
  - Handles the Keycloak callback at the `/keycloack/auth/callback` endpoint.
  - Exchanges the authorization code for tokens (`access_token`, `refresh_token`, `id_token`) and returns this information to the browser along with additional data (`userFirstName`, `userId`).

- **`ResourceServerConfig`**:
  - Configures Spring Security as a Resource Server to validate JWT tokens issued by Keycloak.
  - Defines authorization rules, allowing the `/keycloack/auth/**` endpoint and protecting `/products` with roles (`ROLE_OPERATOR`, `ROLE_ADMIN`).

### `application.properties` Configuration

Keycloak and backend settings are defined in `application.properties`:

```properties
# Keycloak Configuration
keycloak.client-id=dscatalog-client
keycloak.redirect-uri=http://localhost:8080/keycloack/auth/callback
keycloak.token-endpoint=http://localhost:8081/realms/dscatalog-realm/protocol/openid-connect/token
keycloak.issuer-uri=http://localhost:8081/realms/dscatalog-realm
```

---

## Keycloak Setup

For the authentication flow to work, Keycloak must be configured as follows:

1. **Start Keycloak**:
   - Download and install Keycloak (or use Docker: `docker run -p 8081:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:23.0.6 start-dev`).
   - Keycloak should run on port 8081 to avoid conflicts with Spring Boot (port 8080).
   - Access the Keycloak Admin Console at `http://localhost:8081/admin` and log in (user: `admin`, password: `admin`).

2. **Create a Realm**:
   - In the Keycloak Admin Console, create a new realm named `dscatalog-realm`.

3. **Configure the Client**:
   - Create a client with the following settings:
     - **Client ID**: `dscatalog-client`.
     - **Client Authentication**: Disabled (public client, required for PKCE).
     - **Standard Flow**: Enabled (to support the Authorization Code flow).
     - **Valid Redirect URIs**: `http://localhost:8080/keycloack/auth/callback`.
     - **Web Origins**: `http://localhost:8080` (to support CORS).

4. **Configure the User**:
   - Create a user in the `dscatalog-realm`:
     - **Username**: `maria@gmail.com`.
     - **Password**: `123456` (under "Credentials", disable "Temporary").
     - **Roles**: Add the roles `ROLE_OPERATOR` and `ROLE_ADMIN` (under "Role Mapping").
     - **Attributes**: Add the attributes `userFirstName` (value: `Maria`) and `userId` (value: `2`) in the "Details" tab.

5. **Configure Mappers**:
   - In the `dscatalog-client`, go to the "Client Scopes" tab and click on `dscatalog-client-dedicated`.
   - Add mappers to include the `userFirstName` and `userId` attributes in the `access_token`:
     - **Name**: `userFirstName`.
     - **Mapper Type**: `User Attribute`.
     - **Token Claim Name**: `userFirstName`.
     - Repeat for `userId`.

---

## Testing the Authentication

### 1. Authorization Request in the Browser

To start the authentication flow, access the following URL in the browser:

```
http://localhost:8081/realms/dscatalog-realm/protocol/openid-connect/auth?response_type=code&client_id=dscatalog-client&redirect_uri=http://localhost:8080/keycloack/auth/callback&scope=openid&state=abc123&code_challenge=z7cffSHPPrDiMdEtqCVaOu0oznwDP42PGJxUy0kxjUo&code_challenge_method=S256
```

**Notes**:
- The `code_challenge` (`z7cffSHPPrDiMdEtqCVaOu0oznwDP42PGJxUy0kxjUo`) was generated from the `code_verifier` (`MyCustomCodeVerifierWithAtLeast43Characters12345`) using SHA-256 and Base64 URL-safe encoding.
- If there is no active session, Keycloak will display the login screen. Use the credentials `maria@gmail.com` / `123456`.
- After authentication, Keycloak will redirect to the backend, which will return the tokens in JSON format:
  ```json
  {
    "access_token": "...",
    "expires_in": 300,
    "refresh_expires_in": 1800,
    "refresh_token": "...",
    "token_type": "Bearer",
    "id_token": "...",
    "not-before-policy": 1744666694,
    "session_state": "...",
    "scope": "openid email profile",
    "firstName": "Maria",
    "userId": "2"
  }
  ```

### 2. Calling the Paginated `/products` Endpoint with `curl`

The `/products` endpoint is protected and requires an `access_token` with the roles `ROLE_OPERATOR` or `ROLE_ADMIN`. Use the `access_token` obtained in the previous step to make the request.

**Example request with `curl`**:

```bash
curl -X GET "http://localhost:8080/products?page=0&size=13&sort=id,desc&categoryId=2" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvQkdvaENwd0tNOF80dDJXZlBGTlBTZl9DcnNWMWFfNHFFanBmWV94dkdZIn0..."
```

**Parameters**:
- `page=0`: First page (zero-indexed).
- `size=13`: Page size (13 products per page).
- `sort=id,desc`: Sort by `id` in descending order.
- `categoryId=2`: Filters products from category ID 2.

**Expected response** (example):
```json
{
  "content": [
    {
      "id": 25,
      "name": "Sample Product",
      "description": "Product description",
      "price": 99.99,
      "category": { "id": 2, "name": "Sample Category" }
    }
  ],
  "pageable": {
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 13,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 2,
  "totalElements": 20,
  "last": false,
  "size": 13,
  "number": 0,
  "numberOfElements": 13,
  "first": true,
  "empty": false
}
```

**Notes**:
- Make sure the `access_token` is still valid (not expired).
- The `/products` endpoint requires the token to carry the roles `ROLE_OPERATOR` or `ROLE_ADMIN`, which were assigned to `maria@gmail.com`.

---

## Authentication Flow

1. **Authorization Request**: The browser accesses the Keycloak authorization URL (port 8081), which authenticates the user.
2. **Redirect**: Keycloak redirects to the backend (port 8080) with an authorization code.
3. **Token Exchange**: The `AuthController` exchanges the code for tokens, sending the `code_verifier` (PKCE) to Keycloak.
4. **Response to Browser**: The backend returns the tokens along with additional information (`userFirstName`, `userId`).

---

## Reflection

This project was a learning exercise to understand the OAuth 2.0 Authorization Code with PKCE flow and the integration between Keycloak and Spring Security. During development, I faced challenges such as Keycloak configuration errors (e.g.: invalid `redirect_uri`) and Spring Boot issues (e.g.: commented-out variable in `application.properties`). Although the implementation is not production-ready, it was enough to understand the authentication flow and how to modernize a legacy project that used `grant_type=password`.

---

## How to Run the Project

1. **Prerequisites**:
   - Java 17 or higher.
   - Maven.
   - Keycloak (version 23.0.6 or compatible).
   - Docker (optional, to run Keycloak).

2. **Steps**:
   - Clone the repository.
   - Configure Keycloak as described above.
   - Update `application.properties` with your Keycloak settings.
   - Run the Spring Boot application: `mvn spring-boot:run`.
   - Access the authorization URL in the browser to obtain the tokens.
   - Use the `access_token` to call the `/products` endpoint via `curl`.

![sequence diagram](https://github.com/rgiovann/image-repo/blob/main/sd_pkce.jpg)
