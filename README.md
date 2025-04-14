# DS Catalog - Implementação de Autenticação com Keycloak e Spring Boot 3.x

Este projeto faz parte do bootcamp do Nelio Alves e implementa o sistema **DS Catalog**, uma aplicação de catálogo de produtos. Originalmente, o projeto usava **Spring Authorization 2.0** com o fluxo `grant_type=password`, que não é mais recomendado por questões de segurança. Como parte do meu aprendizado, migrei a implementação para **Spring Boot 3.0** e, posteriormente, para o fluxo **OAuth 2.0 Authorization Code com PKCE** integrado ao **Keycloak** como Authorization Server.

Este documento descreve a implementação do fluxo de autenticação, a configuração do Keycloak, e como testar a aplicação, incluindo chamadas autenticadas a endpoints protegidos.

---

## Objetivo

O objetivo principal foi entender o fluxo de autenticação OAuth 2.0 Authorization Code com PKCE, integrando o Keycloak ao Spring Boot 3.x com Spring Security. A implementação foi focada em aprendizado, então há detalhes para produção que ainda não foram abordados (ex.: validação robusta do `state`, geração dinâmica do `code_verifier`).

---

## Estrutura do Projeto

### Classes Principais

- **`AuthController`**:
  - Gerencia o callback do Keycloak no endpoint `/keycloack/auth/callback`.
  - Troca o código de autorização por tokens (`access_token`, `refresh_token`, `id_token`) e retorna essas informações ao navegador com dados adicionais (`userFirstName`, `userId`).

- **`ResourceServerConfig`**:
  - Configura o Spring Security como Resource Server para validar tokens JWT emitidos pelo Keycloak.
  - Define regras de autorização, liberando o endpoint `/keycloack/auth/**` e protegendo `/products` com roles (`ROLE_OPERATOR`, `ROLE_ADMIN`).

### Configurações no `application.properties`

As configurações do Keycloak e do backend são definidas no arquivo `application.properties`:

```properties
# Keycloak Configuration
keycloak.client-id=dscatalog-client
keycloak.redirect-uri=http://localhost:8080/keycloack/auth/callback
keycloak.token-endpoint=http://localhost:8081/realms/dscatalog-realm/protocol/openid-connect/token
keycloak.issuer-uri=http://localhost:8081/realms/dscatalog-realm
```

---

## Configuração do Keycloak

Para que o fluxo de autenticação funcione, é necessário configurar o Keycloak. Siga os passos abaixo:

1. **Inicie o Keycloak**:
   - Baixe e instale o Keycloak (ou use Docker: `docker run -p 8081:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:23.0.6 start-dev`).
   - O Keycloak deve rodar na porta 8081 para evitar conflitos com o Spring Boot (porta 8080).
   - Acesse o Keycloak Admin Console em `http://localhost:8081/admin` e faça login (usuário: `admin`, senha: `admin`).

2. **Crie um Realm**:
   - No Keycloak Admin Console, crie um novo realm chamado `dscatalog-realm`.

3. **Configure o Cliente**:
   - Crie um cliente com as seguintes configurações:
     - **Client ID**: `dscatalog-client`.
     - **Client Authentication**: Desativado (cliente público, necessário para PKCE).
     - **Standard Flow**: Habilitado (para suportar o fluxo Authorization Code).
     - **Valid Redirect URIs**: `http://localhost:8080/keycloack/auth/callback`.
     - **Web Origins**: `http://localhost:8080` (para suportar CORS).

4. **Configure o Usuário**:
   - Crie um usuário no realm `dscatalog-realm`:
     - **Username**: `maria@gmail.com`.
     - **Password**: `123456` (em "Credentials", desative "Temporary").
     - **Roles**: Adicione as roles `ROLE_OPERATOR` e `ROLE_ADMIN` (em "Role Mapping").
     - **Atributos**: Adicione os atributos `userFirstName` (valor: `Maria`) e `userId` (valor: `2`) na aba "Details".

5. **Configure os Mappers**:
   - No cliente `dscatalog-client`, vá para a aba "Client Scopes" e clique em `dscatalog-client-dedicated`.
   - Adicione mappers para incluir os atributos `userFirstName` e `userId` no `access_token`:
     - **Name**: `userFirstName`.
     - **Mapper Type**: `User Attribute`.
     - **Token Claim Name**: `userFirstName`.
     - Repita para o `userId`.

---

## Como Testar a Autenticação

### 1. Requisição de Autorização no Navegador

Para iniciar o fluxo de autenticação, acesse a seguinte URL no navegador:

```
http://localhost:8081/realms/dscatalog-realm/protocol/openid-connect/auth?response_type=code&client_id=dscatalog-client&redirect_uri=http://localhost:8080/keycloack/auth/callback&scope=openid&state=abc123&code_challenge=z7cffSHPPrDiMdEtqCVaOu0oznwDP42PGJxUy0kxjUo&code_challenge_method=S256
```

**Notas**:
- O `code_challenge` (`z7cffSHPPrDiMdEtqCVaOu0oznwDP42PGJxUy0kxjUo`) foi gerado a partir do `code_verifier` (`MyCustomCodeVerifierWithAtLeast43Characters12345`) usando SHA-256 e codificação Base64 URL-safe.
- Se não houver uma sessão ativa, o Keycloak exibirá a tela de login. Use as credenciais `maria@gmail.com` / `123456`.
- Após a autenticação, o Keycloak redirecionará para o backend, que retornará os tokens no formato JSON:
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

### 2. Chamada ao Endpoint Paginado `/products` com `curl`

O endpoint `/products` é protegido e requer um `access_token` com roles `ROLE_OPERATOR` ou `ROLE_ADMIN`. Use o `access_token` obtido na etapa anterior para fazer a requisição.

**Exemplo de Requisição com `curl`**:

```bash
curl -X GET "http://localhost:8080/products?page=0&size=13&sort=id,desc&categoryId=2" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvQkdvaENwd0tNOF80dDJXZlBGTlBTZl9DcnNWMWFfNHFFanBmWV94dkdZIn0.eyJleHAiOjE3NDQ2Njk1OTYsImlhdCI6MTc0NDY2OTI5NiwiYXV0aF90aW1lIjoxNzQ0NjY5Mjk2LCJqdGkiOiI4NmVlMjRiOS02YzA5LTQ1YTItYjM1ZC02NzMzZWE0Yjc3ZjMiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODEvcmVhbG1zL2RzY2F0YWxvZy1yZWFsbSIsInN1YiI6Im1hcmlhQGdtYWlsLmNvbSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImRzY2F0YWxvZy1jbGllbnQiLCJzaWQiOiJlZDI1MTBkZi0wNDQ2LTQ1ZjUtOTJiMC02YmE0ZTM5NGY4MjAiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiUk9MRV9PUEVSQVRPUiIsIlJPTEVfQURNSU4iXX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6Ik1hcmlhIGRhcyBEb3JlcyIsInVzZXJGaXJzdE5hbWUiOiJNYXJpYSIsInByZWZlcnJlZF91c2VybmFtZSI6Im1hcmlhQGdtYWlsLmNvbSIsImdpdmVuX25hbWUiOiJNYXJpYSIsInVzZXJJZCI6IjIiLCJmYW1pbHlfbmFtZSI6ImRhcyBEb3JlcyIsImVtYWlsIjoibWFyaWFAZ21haWwuY29tIn0.UKRPRUPj7tlNER74jdjkbi8YhRY9Da_BxHguyDe2kj11WCa8jCWSFIpUl-W-lvu_ShlcvKtFbb-fIR_DvOYP7Q7ztd3gOEzGD4r9-kePeUHeKbxMNLN-l2-vPBFJVeqAclhOP7MunkX13V8mL6q0nmFhb2XwbdaRBH8vWOgo0-syHRjNAJr_DTRyXSMLDDz2bsbfTg5qbc52egQYuZFmOHAl74RkWXSbBXm3ThQlcQZC-bLEX5zkuXd25zF5SYL59YfH1exoBVZ8rg8Sjx6ucw7cw5PpQs-7nMqFhoFDtY73a4pvzFQ2sjSKh9hG3DtRaIT5hv3QNcPhOMuBF9njqw"
```

**Parâmetros**:
- `page=0`: Página inicial (começa em 0).
- `size=13`: Tamanho da página (13 produtos por página).
- `sort=id,desc`: Ordenação por `id` em ordem decrescente.
- `categoryId=2`: Filtra produtos da categoria com ID 2.

**Resposta Esperada** (exemplo):
```json
{
  "content": [
    {
      "id": 25,
      "name": "Produto Exemplo",
      "description": "Descrição do produto",
      "price": 99.99,
      "category": { "id": 2, "name": "Categoria Exemplo" }
    },
    // Mais produtos...
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
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "numberOfElements": 13,
  "first": true,
  "empty": false
}
```

**Notas**:
- Certifique-se de que o `access_token` está válido (não expirado).
- O endpoint `/products` exige que o token tenha as roles `ROLE_OPERATOR` ou `ROLE_ADMIN`, que foram configuradas para o usuário `maria@gmail.com`.

---

## Fluxo de Autenticação

1. **Requisição de Autorização**: O navegador acessa a URL de autorização do Keycloak (porta 8081), que autentica o usuário.
2. **Redirecionamento**: O Keycloak redireciona para o backend (porta 8080) com um código de autorização.
3. **Troca de Tokens**: O `AuthController` troca o código por tokens, enviando o `code_verifier` (PKCE) ao Keycloak.
4. **Resposta ao Navegador**: O backend retorna os tokens e informações adicionais (`userFirstName`, `userId`).

---

## Reflexão

Esse projeto foi um exercício de aprendizado para entender o fluxo OAuth 2.0 Authorization Code com PKCE e a integração entre Keycloak e Spring Security. Durante o desenvolvimento, enfrentei desafios como erros de configuração no Keycloak (ex.: `redirect_uri` inválido) e no Spring Boot (ex.: variável comentada no `application.properties`). Embora a implementação não esteja pronta para produção, foi suficiente para compreender o fluxo de autenticação e como modernizar um projeto antigo que usava `grant_type=password`.



## Como Executar o Projeto

1. **Pré-requisitos**:
   - Java 17 ou superior.
   - Maven.
   - Keycloak (versão 23.0.6 ou compatível).
   - Docker (opcional, para rodar o Keycloak).

2. **Passos**:
   - Clone o repositório.
   - Configure o Keycloak conforme as instruções acima.
   - Atualize o `application.properties` com as configurações do Keycloak.
   - Execute a aplicação Spring Boot: `mvn spring-boot:run`.
   - Acesse a URL de autorização no navegador para obter os tokens.
   - Use o `access_token` para chamar o endpoint `/products` via `curl`.


![diagrama de sequência](https://github.com/rgiovann/image-repo/blob/main/sd_pkce.jpg)